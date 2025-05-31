package com.hbk619.intellisay.audiocue

import com.hbk619.intellisay.audiocue.AudioCueFunctions.PanType
import com.hbk619.intellisay.audiocue.AudioCueFunctions.VolType
import java.io.IOException
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque
import java.util.function.Function
import javax.sound.sampled.*
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.math.min

/**
 * The `AudioCue` class functions as a data line, where the
 * audio data played directly from memory. `AudioCue` is modeled
 * upon `javax.sound.sampled.Clip` but with additional capabilities.
 * It can play multiple instances concurrently, and offers individual
 * dynamic controls for volume, panning and speed for each concurrently
 * playing instance.
 *
 *
 * An `AudioCue` is created using the static factory method
 * `makeAudioCue`. When doing so, the media data is loaded
 * either from a "CD Quality" wav file (44100 fps, 16-bit, stereo,
 * little-endian) or a float array of stereo PCM normalized to the
 * range -1 to 1, at 44100 fps. Once loaded, the media data is
 * immutable.
 *
 *
 * `AudioCue` achieves concurrent playback by treating state at
 * two levels. A distinction is made between the `AudioCue`
 * which holds the audio data, and a playback *instance* which
 * controls a cursor that traverses over that data and corresponds to
 * a single sounding playback. Methods that dynamically affect
 * playback (volume, pan, speed) operate at the instance level. When
 * using the factory method to create an `AudioCue`, the
 * maximum number of simultaneously playing instances to be supported
 * is set, along with the data source.
 *
 *
 * An `AudioCue` is either open or closed. Upon opening, an
 * output line is obtained from the system. That line will either be
 * held directly by the `AudioCue` or provided indirectly by an
 * `AudioMixer`, in which case 'opening' refers to the act
 * of registering as an `AudioMixerTrack` with an
 * `AudioMixer`. Upon closing, the `AudioCue` releases the
 * system audio line, or if registered with an `AudioMixer`,
 * unregisters. Opening and closing events are broadcast to registered
 * listeners implementing the methods
 * `AudioCueListener.audioCueOpened` and
 * `AudioCueListener.audioCueClosed`.
 *
 *
 * The line used for output is a
 * `javax.sound.sampled.SourceDataLine`. The format specified
 * for the line is 44100 fps, 16-bit, stereo, little-endian, a format
 * also known as "CD Quality". This format is one of the most widely
 * supported and is the only format available for output via
 * `AudioCue`. If a `javax.sound.sampled.Mixer` is not
 * specified, the default behavior is to obtain the line from the
 * system's default `Mixer`, with a buffer size of 1024 frames
 * (4192 bytes). The line will be run from within a dedicated thread,
 * with a thread priority of `HIGHEST`. Given that the
 * processing of audio data usually progresses much faster than time
 * it takes to play the media, this thread can be expected to spend
 * most of its time in a blocked state, which allows maximizing the
 * thread priority without impacting overall performance. An
 * alternative `Mixer`, buffer length or thread priority can be
 * specified as parameters to the `open` method.
 *
 *
 * An *instance* can either be *active* or not active,
 * and, if active, can either be *running* or not running.
 * Instances are initially held in a pool as 'available instances'.
 * An instance becomes active when it is removed from that pool.
 * Methods which remove an instance from the pool either return an
 * `int` identifier or -1 if there are no available instances.
 * An active instance can receive commands pertaining to the cursor's
 * location in the audio data, to starting or stopping, as well as
 * volume, pan, and speed changes, and other commands. An inactive
 * instance can only receive a command that withdraws it from the
 * 'available instances' pool. An instance that is *running*
 * is one that is currently being played.
 *
 *
 * Each instance holds its own state variables for volume, pan and
 * speed, cursor location, and looping. Methods are provided for
 * changing these values. If the methods are called while the
 * instance is not running, the new value is stored immediately and
 * will take affect when the instance is restarted. If the instance
 * is running, and the change is for volume, pan, or speed, the new
 * value will be arrived at incrementally, with the increments
 * occurring behind the scenes on a per frame basis. This is done to
 * prevent discontinuities in the signal that might result in audible
 * clicks. If a running instance's property is updated before the
 * incremental changes have completed, the new target value takes
 * precedence, and a new increment is created based on the current,
 * in-progress value.
 *
 *
 * An instance may be played on a *fire-and-forget* basis, in
 * which case it is automatically returned to the pool of available
 * instances (active == false). Alternatively, the instance can be
 * allowed to stay active upon completion. The behavior upon play
 * completion is controlled by the public method
 * `setRecycleWhenDone(int, boolean)`, where `int` is the
 * instance identifier. When a `play` method is called, the
 * `recycledWhenDone` value is set to `true` by default.
 * When `obtainInstance` is used, the `recycledWhenDone`
 * value is set to `false`.
 *
 *
 * **Examples**: (assumes the audioCue is open)
 * <pre>`    // (1) Fire-and-Forget, with default values.
 * audioCue.play(); // (vol = 1, pan = 0, speed = 1, loop = 0)
 * // instance will be recycled upon completion.
 *
 * // (2) Fire-and-forget, with explicit values
 * int play0 = audioCue.play(0.5, -0.75, 2, 1);
 * // will play at "half" volume, "half way" to the left pan,
 * // double the speed, and will loop back and play again one time.
 * // Instance will be recycled upon completion, however, the
 * // play0 variable allows one to make further changes to the
 * // instance if executed prior to the completion of the
 * // playback.
 *
 * // (3) Using obtainInstance()
 * int footstep = footstepCue.obtainInstance();
 * for (int i = 0; i &#60; 10; i++) {
 * // play the successive footsteps more quietly
 * footstepCue.setVolume(footstep, 1 - (i * 0.1));
 * // successive footsteps will travel from left to right
 * footstepCue.setPan(footstep, -1 + (i * 0.2));
 * // cursor must be manually placed at the beginning
 * footstepCue.setFramePosition(footstep, 0);
 * footstepCue.start(footstep);
 * Thread.sleep(1000); // Allow time between each footstep start.
 * // This assumes that the cue is shorter than 1 second in
 * // in length and each start will stop on its own.
 * }
 * // Another instance might be associated with a slightly
 * // different speed, for example 1.1, implying a different
 * // individual with a slightly lighter foot fall.
`</pre> *
 *
 *
 * More extensive examples can be found in the companion github project
 * *audiocue-demo*.
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 */
class AudioCue private constructor(
    private val cue: FloatArray,
    /**
     * Sets the name of the `AudioCue`.
     *
     * @param name - a `String` to associate with this
     * `AudioCue`
     */
    var name: String?, private val polyphony: Int
) : AudioMixerTrack, AutoCloseable {
    private val availables: LinkedBlockingDeque<AudioCueCursor?>
    private val cueFrameLength: Int
    private val cursors: ArrayList<AudioCueCursor>

    private var player: AudioCuePlayer? = null
    private var audioMixer: AudioMixer? = null

    private var playerRunning = false

    @Volatile
    private var trackRunning = false
    private var readBuffer: FloatArray


    /**
     * Returns the name associated with the `AudioCue`.
     * @return the name as a `String`
     */

    private val listeners: CopyOnWriteArrayList<AudioCueListener>

    /**
     * Registers an `AudioCueListener` to receive
     * notifications of events pertaining to the `AudioCue`
     * and its playing or playable instances.
     *
     *
     * Notifications occur on the thread which processes the
     * audio signal, and includes events such as the starting,
     * stopping, and looping of instances. Implementations of
     * the methods that receive these notifications should be
     * coded for brevity in order to minimize extraneous
     * processing on the audio thread.
     *
     * @param listener - an object implementing the
     * `AudioCueListener` interface
     */
    fun addAudioCueListener(listener: AudioCueListener?) {
        listeners.add(listener)
    }

    /**
     * Removes an `AudioCueListener` from receiving
     * notifications of events pertaining to the `AudioCue`
     * and its playing instances.
     *
     * @param listener - an object implementing the
     * `AudioCueListener` interface
     */
    fun removeAudioCueListener(listener: AudioCueListener?) {
        listeners.remove(listener)
    }

    private var vol: Function<Float?, Float?>? = null

    /**
     * Assigns a function to map linear volume settings to volume
     * factors used to control the sound cue's amplitude.
     *
     * @param volType - a member of the `enum AudioCue.VolType`
     * @see VolType
     */
    fun setVolType(volType: VolType) {
        vol = volType.vol
    }

    private var panL: Function<Float?, Float?>? = null
    private var panR: Function<Float?, Float?>? = null

    /**
     * Assigns the type of panning to be used.
     *
     * @param panType - a member of the `enum AudioCue.PanType`
     * @see PanType
     */
    fun setPanType(panType: PanType) {
        panL = panType.left
        panR = panType.right
    }

    /**
     * Private constructor, used internally.
     *
     * @param cue       - a `float` array of stereo, signed, normalized PCM
     * @param name      - a `String` to be associated with the
     * `AudioCue`
     * @param polyphony - an `int` specifying the maximum number of
     * concurrent instances
     */
    init {
        this.cueFrameLength = cue.size / 2

        availables = LinkedBlockingDeque<AudioCueCursor?>(polyphony)
        cursors = arrayListOf<AudioCueCursor>()

        for (i in 0..<polyphony) {
            cursors.add(AudioCueCursor(i))
            cursors[i].resetInstance()
            availables.add(cursors[i])
        }


        // default readBuffer
        readBuffer = FloatArray(DEFAULT_BUFFER_FRAMES * 2)


        // default volume function
        setVolType(VolType.EXP_X4)


        // default pan calculation function
        setPanType(PanType.SINE_LAW)

        listeners = CopyOnWriteArrayList<AudioCueListener>()
    }

    val pcmCopy: FloatArray?
        /**
         * Returns a copy of the signed, normalized float PCM array for this
         * `AudioCue`.
         *
         * @return a `float[]` new copy of the internal array of the
         * PCM for the `AudioCue`
         */
        get() = cue.copyOf(cue.size)

    /**
     * Readies this `AudioCue` for media play by instantiating, registering,
     * and running the inner class `AudioCuePlayer` with a custom buffer
     * size. As the performance of the AudioCuePlayer is subject to tradeoffs
     * based upon the size of an internal buffer, a number of frames other than
     * the default of 1024 can be specified with this method. A lower value responds
     * more quickly to dynamic requests, but is more prone to underflow which can
     * result in audible drop outs. The registered `AudioCuePlayer` instance
     * obtains and configures a `javax.sound.sampled.SourceDataLine` to write
     * to the default system `javax.sound.sampled.Mixer`, and will run with
     * the default thread priority setting of 10.
     *
     *
     * Once completed, this `AudioCue` is marked open and the
     * `AudioCueListener.audioCueOpened` method is called on every registered
     * `AudioCueListener`.
     *
     *
     * NOTE: data *can* be read from an `AudioCue` even if it is not open. The
     * data that is read from an unopened `AudioCue` will not be written to
     * a `SourceDataLine` and will not be heard.
     *
     * @param bufferFrames - an `int` specifying the size of the internal
     * buffer in PCM frames
     * @throws IllegalStateException if this `AudioCue` is already open
     * @throws LineUnavailableException if unable to obtain a `SourceDataLine`
     * for the player, which could occur if the Mixer does
     * not support the `AudioFormat` implemented by
     * `AudioCue`
     * @see .audioFormat
     *
     * @see .open
     * @see AudioCueListener.audioCueOpened
     */
    @Throws(LineUnavailableException::class)
    fun open(bufferFrames: Int) {
        open(null, bufferFrames, Thread.MAX_PRIORITY)
    }

    /**
     * Readies this `AudioCue` for media play by instantiating, registering,
     * and running the inner class `AudioCuePlayer` with explicit settings
     * for the `javax.sound.sampled.Mixer`, the number of PCM frames used
     * in an internal buffer, and the priority level for the thread created to
     * handle the media play. Internally, the registered `AudioCuePlayer`
     * instance obtains and configures a `javax.sound.sampled.SourceDataLine`
     * to write to the provided `javax.sound.sampled.Mixer` rather than the
     * default system Mixer. As the performance of the AudioCuePlayer is subject
     * to tradeoffs based upon the size of an internal buffer, a number of frames
     * other than the default of 1024 can be specified. A lower value responds
     * more quickly to dynamic requests, but is more prone to underflow which can
     * result in audible drop outs. While the default thread priority setting of
     * 10 is generally safe given that to audio processing spending a majority of
     * its time in a blocked state, this method allows specification of a lower
     * priority setting.
     *
     *
     * Once completed, this `AudioCue` is marked open and the
     * `audioCueOpened` method is called on every registered
     * `AudioCueListener`.
     *
     *
     * NOTE: data *can* be read from an `AudioCue` even if it is not open. The
     * data that is read from an unopened `AudioCue` will not be written to
     * a `SourceDataLine` and will not be heard.
     *
     * @param mixer          - a `javax.sound.sampled.Mixer`. If `null`,
     * the system default mixer is used.
     * @param bufferFrames   - an `int` specifying the size of the internal
     * buffer in PCM frames
     * @param threadPriority - an `int` specifying the priority level of the
     * thread, clamped to the range 1 to 10 inclusive
     * @throws IllegalArgumentException if the thread priority is not in the range
     * MIN_PRIORITY to MAX_PRIORITY.
     * @throws IllegalStateException if this `AudioCue` is already open
     * @throws LineUnavailableException if unable to obtain a `SourceDataLine`
     * for the player, which could occur if the Mixer does
     * not support the `AudioFormat` implemented by
     * `AudioCue`
     * @see .audioFormat
     *
     * @see AudioCueListener
     *
     * @see Mixer
     *
     * @see AudioCueListener.audioCueOpened
     */
    /**
     * Readies this `AudioCue` for media play by instantiating, registering,
     * and running the inner class `AudioCuePlayer` with default settings.
     * Internally, the registered `AudioCuePlayer` instance obtains and
     * configures a `javax.sound.sampled.SourceDataLine` to write to the
     * default system `javax.sound.sampled.Mixer`, and will make use of the
     * default internal buffer size of 1024 PCM frames, and will run with the
     * default thread priority setting of 10.
     *
     *
     * Once completed, this `AudioCue` is marked open and the
     * `AudioCueListener.audioCueOpened` method is called on every registered
     * `AudioCueListener`.
     *
     *
     * NOTE: data *can* be read from an `AudioCue` even if it is not open. The
     * data that is read from an unopened `AudioCue` will not be written to
     * a `SourceDataLine` and will not be heard.
     *
     * @throws IllegalStateException if this `AudioCue` is already open
     * @throws LineUnavailableException if unable to obtain a `SourceDataLine`
     * for the player, which could occur if the Mixer does
     * not support the `AudioFormat` implemented by
     * `AudioCue`
     * @see .audioFormat
     *
     * @see .open
     * @see AudioCueListener.audioCueOpened
     */
    @JvmOverloads
    @Throws(LineUnavailableException::class)
    fun open(
        mixer: Mixer? = null,
        bufferFrames: Int = DEFAULT_BUFFER_FRAMES,
        threadPriority: Int = Thread.MAX_PRIORITY
    ) {
        check(!playerRunning) { "Already open." }

        require(
            !(threadPriority < Thread.MIN_PRIORITY
                    || threadPriority > Thread.MAX_PRIORITY)
        ) { "Thread priority out of range." }

        player = AudioCuePlayer(mixer, bufferFrames)
        val t = Thread(player)

        t.setPriority(threadPriority)
        playerRunning = true
        t.start()

        broadcastOpenEvent(t.getPriority(), bufferFrames, name)
    }

    /**
     * Registers an `AudioMixer`, instead of an inner class
     * `AudioCuePlayer`, to handle the media play. This
     * `AudioCue` will be added as an `AudioMixerTrack`
     * to the registered `AudioMixer`, and the buffer size and
     * the thread priority of the `AudioMixer` will take effect
     * for media play.
     *
     *
     * Once completed, the `AudioCue` is marked open and the
     * `AudioCueListener.audioCueOpened` method is called on every
     * registered `AudioCueListener`.
     *
     *
     * NOTE: data *can* be read from an `AudioCue` even if it is not open. The
     * data that is read from an unopened `AudioCue` will not be written to
     * a `SourceDataLine` and will not be heard.
     *
     * @param audioMixer - the `AudioMixer` that will handle media output
     * for this `AudioCue`
     * @throws IllegalStateException if the `AudioCue` is already open
     * @see AudioMixer
     *
     * @see AudioCueListener.audioCueOpened
     */
    fun open(audioMixer: AudioMixer) {
        check(!playerRunning) { "Already open." }
        playerRunning = true
        // default: AudioCueTrack is open
        trackRunning = true
        this.audioMixer = audioMixer


        // assigned size is frames * stereo
        readBuffer = FloatArray(audioMixer.bufferFrames * 2)

        audioMixer.addTrack(this)
        audioMixer.updateTracks()

        broadcastOpenEvent(
            audioMixer.threadPriority,
            audioMixer.bufferFrames, name
        )
    }

    /**
     * Releases resources allocated for media play. If the `AudioCue`
     * was opened as a stand alone cue, its inner class `AudioPlayer`
     * runnable will be allowed to end, and allocated resources will be
     * cleaned up. If the `AudioCue` was opened as a track on an
     * `AudioMixer`, the track will be removed from the `AudioMixer`.
     *
     *
     * Once completed, the `AudioCue` is marked closed and the
     * `AudioCueListener.audioCueClosed` method is called on every
     * registered `AudioCueListener`.
     *
     * @throws IllegalStateException if player is already closed
     * @see AudioCueListener.audioCueClosed
     */
    override fun close() {
        check(playerRunning != false) { "Already closed." }

        if (audioMixer != null) {
            audioMixer!!.removeTrack(this)
            audioMixer!!.updateTracks()
            audioMixer = null
        } else {
            // allows player thread to end
            player!!.stopRunning()
        }

        playerRunning = false

        broadcastCloseEvent(name)
    }

    val frameLength: Long
        /**
         * Gets the media length in sample frames.
         *
         * @return length in sample frames
         */
        get() = cueFrameLength.toLong()

    val microsecondLength: Long
        /**
         * Gets the media length in microseconds.
         *
         * @return length in microseconds
         */
        get() = ((cueFrameLength * 1000000.0)
                / audioFormat.getFrameRate()).toLong()


    /**
     * Obtains an `int` instance identifier from a pool of
     * available instances and marks this instance as 'active'. If
     * no playable instances are available, the method returns -1.
     *
     *
     * The instance designated by this identifier is
     * *not* automatically recycled back into
     * the pool of available instances when it finishes playing.
     * To put the instance back in the pool of available instances,
     * the method `releaseInstance` must be called. To
     * change the behavior so that the instance *is*
     * returned to the pool when it plays completely to the end,
     * use `setRecycleWhenDone(int, boolean)`.
     *
     *
     * When executed, the `AudioCueListener.instanceEventOccurred`
     * method will be called with an argument of
     * `AudioCueInstanceEvent.Type.OBTAIN_INSTANCE`.
     *
     * @return an `int` instance ID for an active instance,
     * or -1 if no instances are available
     * @see .releaseInstance
     * @see .setRecycleWhenDone
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.OBTAIN_INSTANCE
     */
    fun obtainInstance(): Int {
        val acc = availables.pollLast()

        if (acc == null) return -1
        else {
            acc.isActive = true
            broadcastCreateInstanceEvent(acc)
            return acc.id
        }
    }

    /**
     * Releases an `AudioCue` instance, making  it available
     * as a new concurrently playing instance. Once released, and
     * back in the pool of available instances, an instance cannot
     * receive updates.
     *
     *
     * When executed, the `AudioCueListener.instanceEventOccurred`
     * method will be called with an argument of
     * `AudioCueInstanceEvent.Type.RELEASE_EVENT`.
     *
     * @param instanceID - an `int` identifying the instance
     * to be released
     * @see .obtainInstance
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.RELEASE_INSTANCE
     */
    fun releaseInstance(instanceID: Int) {
        cursors[instanceID].resetInstance()
        availables.offerFirst(cursors[instanceID])
        broadcastReleaseEvent(cursors[instanceID])
    }

    /**
     * Obtains an `AudioCue` instance and, if the `AudioCue` has
     * been *opened*, starts the cue playing in its own thread, from
     * the beginning, at the given volume, pan, speed and number of repetitions,
     * and returns an `int` identifying the instance, or, returns -1 if
     * no `AudioCue` instance is available.
     *
     *
     * If an `AudioCue` instance is available for play back,
     * the `AudioCueListener.instanceEventOccurred` method
     * will be called twice, first with the argument
     * `AudioCueInstanceEvent.Type.OBTAIN_INSTANCE` and then
     * with `AudioCueInstanceEvent.Type.START_INSTANCE`. This
     * instance will be set to automatically recycle back into the
     * pool of available instances when playing completes.
     *
     *
     * NOTE: the `play` method *can* be called on an
     * unopened `AudioCue`. If unopened, the
     * `AudioCuePlayer.readTrack` method will advance the
     * `AudioCueCursor` and return a buffer of PCM data when
     * called, but will not write the data to the sound system.
     *
     * @param volume - a `double` within the range [0, 1]
     * @param pan    - a `double` within the range [-1, 1]
     * @param speed  - a `double` factor that is multiplied
     * to the frame rate
     * @param loop   - an `int` that specifies a number of
     * *additional* plays (looping)
     * @return an `int` identifying the playing instance,
     * or -1 if no instance is available
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.OBTAIN_INSTANCE
     *
     * @see AudioCueInstanceEvent.Type.START_INSTANCE
     */
    /**
     * Obtains an `AudioCue` instance and, if the `AudioCue` has
     * been *opened*, starts the cue playing in its own thread, from
     * the beginning, with default values: full volume, center pan and at
     * normal speed, and immediately returns an `int` identifying the
     * instance, or, returns -1 if no `AudioCue` instance is available.
     *
     *
     * If an `AudioCue` instance is available to play, the
     * `AudioCueListener.instanceEventOccurred` method
     * will be called twice, first with the argument
     * `AudioCueInstanceEvent.Type.OBTAIN_INSTANCE` and
     * then with `AudioCueInstanceEvent.Type.START_INSTANCE`.
     * This instance will be set to automatically recycle back into
     * the pool of available instances when playing completes.
     *
     *
     * NOTE: the `play` method *can* be called on an
     * unopened `AudioCue`. If unopened, calling the
     * `AudioCuePlayer.readTrack` method will advance the
     * `AudioCueCursor` and return a buffer of PCM data, but
     * will not write the data to the sound system.
     *
     * @return an `int` identifying the playing instance,
     * or -1 if no instance is available
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.OBTAIN_INSTANCE
     *
     * @see AudioCueInstanceEvent.Type.START_INSTANCE
     */
    /**
     * Obtains an `AudioCue` instance and, if the `AudioCue` has
     * been *opened*, starts the cue playing in its own thread, from
     * the beginning, at the given volume, at the default center pan, and at
     * the default normal speed, and immediately returns an `int`
     * identifying the instance, or, returns -1 if no `AudioCue` instance
     * is available.
     *
     *
     * If an `AudioCue` instance is available to play, the
     * `AudioCueListener.instanceEventOccurred` method
     * will be called twice, first with the argument
     * `AudioCueInstanceEvent.Type.OBTAIN_INSTANCE` and then
     * with `AudioCueInstanceEvent.Type.START_INSTANCE`. This
     * instance will be set to automatically recycle back into the
     * pool of available instances when playing completes.
     *
     *
     * NOTE: the `play` method *can* be called on an
     * unopened `AudioCue`. If unopened, the
     * `AudioCuePlayer.readTrack` method will advance the
     * `AudioCueCursor` and return a buffer of PCM data when
     * called, but will not write the data to the sound system.
     *
     * @param volume - a `double` in the range [0, 1]
     * @return an `int` identifying the playing instance,
     * or -1 if no instance is available
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.OBTAIN_INSTANCE
     *
     * @see AudioCueInstanceEvent.Type.START_INSTANCE
     */
    @JvmOverloads
    fun play(volume: Double = 1.0, pan: Double = 0.0, speed: Double = 1.0, loop: Int = 0): Int {
        val idx = obtainInstance()
        if (idx < 0) {
            println("All available notes are playing.")
            return idx
        }

        setVolume(idx, volume)
        setPan(idx, pan)
        setSpeed(idx, speed)
        setLooping(idx, loop)
        setRecycleWhenDone(idx, true)

        start(idx)

        return idx
    }

    /**
     * Launches the playback of the specified `AudioCue` instance
     * from its current position in the data, using existing volume, pan,
     * speed and number of repetitions. Returns immediately.
     *
     *
     * If an `AudioCue` instance is able to start, the
     * `AudioCueListener.instanceEventOccurred` method
     * will be ca	lled with the argument
     * `AudioCueInstanceEvent.Type.START_INSTANCE`. When the
     * playback ends, the `AudioCue` instance is *not*
     * recycled, but instead stops running and the current position
     * remains at the end point of the playback. This method does
     * *not* automatically reposition the cursor to the
     * beginning of the cue, so, if the goal is to restart from the
     * beginning, a repositioning method (such as
     * `setFramePosition` must first be used to move the cursor.
     *
     *
     * If the `AudioCue` has not been *opened*, calls
     * to `AudioMixerTrack.readTrack()` can be used to advance
     * the cursors and produce a buffer-length float[] array of the
     * mix of all the playing instances without sending the data on
     * to a `SourceDataLine` to be heard.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @throws IllegalStateException if instance is not active
     * or if instance is already playing
     *
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.START_INSTANCE
     *
     * @see AudioMixerTrack.readTrack
     */
    fun start(instanceID: Int) {
        check(
            !(!cursors[instanceID].isActive ||
                    cursors[instanceID].isPlaying)
        ) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }
        cursors[instanceID].instantaneousUpdate()
        cursors[instanceID].isPlaying = true
        broadcastStartEvent(cursors[instanceID])
    }

    /**
     * Sends message to indicate that the playing of the instance
     * associated with the `int` identifier should be halted.
     * Calling this method on an already stopped instance does
     * nothing. The instance is left in an open state and is
     * *not* recycled back into the pool of available
     * instances.
     *
     *
     * The `AudioCueListener.instanceEventOccurred` method
     * will be called with the argument
     * `AudioCueInstanceEvent.Type.STOP_INSTANCE`. When the
     * `start` method is called on an instance that has been
     * stopped, playback begins from the stopped cursor location.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @throws IllegalStateException if instance is not active
     * @see AudioCueListener.instanceEventOccurred
     * @see AudioCueInstanceEvent.Type.STOP_INSTANCE
     */
    fun stop(instanceID: Int) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].isPlaying = false
        broadcastStopEvent(cursors[instanceID])
    }

    /**
     * Returns the current sample frame number. The frame count
     * is zero-based. The position may lie in between two frames.
     *
     *
     * An instance cannot have its position read if it is
     * currently playing. An attempt to do so will throw an
     * `IllegalStateException`.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @return a `double` corresponding to the current
     * sample frame position
     * @throws IllegalStateException if instance is not active
     * @see .setFramePosition
     * @see .setMicrosecondPosition
     * @see .setFractionalPosition
     */
    fun getFramePosition(instanceID: Int): Double {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        return cursors[instanceID].cursor
    }

    /**
     * Sets the play position ("play head") to a specified
     * sample frame location. The frame count is zero-based.
     * The play position can be a fractional amount, lying
     * between two frames.
     *
     *
     * The input frame position will be clamped to a value that
     * lies at or within the start and end of the media data. When
     * the instance is next started, it will commence from this
     * position.
     *
     *
     * An instance cannot have its position changed if it is
     * currently playing. An attempt to do so will throw an
     * `IllegalStateException`.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @param frame      - a `double` giving the frame position
     * from which play will commence if the
     * `start` method is executed
     * @throws IllegalStateException if instance is not active
     * or if the instance is playing
     * @see .getFramePosition
     */
    fun setFramePosition(instanceID: Int, frame: Double) {
        check(
            !(!cursors[instanceID].isActive ||
                    cursors[instanceID].isPlaying)
        ) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].cursor = max(
            0f, min(
                (this.frameLength - 1).toFloat(), frame.toFloat()
            )
        ).toDouble()
    }

    /**
     * Repositions the play position ("play head") of the
     * designated `AudioCue` instance to the frame that
     * corresponds to the specified elapsed time in microseconds.
     * The new play position can be a fractional amount, lying
     * between two frames.
     *
     *
     * The input microsecond position will be clamped to a
     * frame that lies within or is located at the start or end
     * of the media data. When the instance is next started, it
     * will commence from this position.
     *
     *
     * An instance cannot have its position changed if it is
     * currently playing. An attempt to do so will throw an
     * `IllegalStateException`.
     *
     * @param instanceID   - an `int` used to identify an
     * `AudioCue` instance
     * @param microseconds - an `int` in microseconds that
     * corresponds to a position in the
     * audio media
     * @throws IllegalStateException if instance is not active
     * or if instance is playing
     * @see .getFramePosition
     */
    fun setMicrosecondPosition(
        instanceID: Int,
        microseconds: Int
    ) {
        check(
            !(!cursors[instanceID].isActive ||
                    cursors[instanceID].isPlaying)
        ) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        val frames = ((audioFormat.getFrameRate() * microseconds)
                / 1000000f)
        cursors[instanceID].cursor = max(0f, min(cueFrameLength.toFloat(), frames)).toDouble()
    }

    /**
     * Repositions the play position ("play head") of the
     * designated `AudioCue` instance to the frame that
     * corresponds to the specified elapsed fractional
     * part the total audio cue length. The new play position can
     * be a fractional amount, lying between two frames.
     *
     *
     * The fractional position argument is clamped to the range
     * [0..1], where 1 corresponds to 100% of the media.  When
     * restarted, the instance will commence from the new
     * sample frame position.
     *
     *
     * An instance cannot have its position changed if it is
     * currently playing. An attempt to do so will throw an
     * `IllegalStateException`.
     *
     * @param instanceID - an `int` used to identify the
     * `AudioCue` instance
     * @param normal     - a `double` in the range [0..1]
     * that corresponds to a position in
     * the media
     * @throws IllegalStateException if instance is not active
     * or if instance is playing
     * @see .getFramePosition
     */
    fun setFractionalPosition(instanceID: Int, normal: Double) {
        check(
            !(!cursors[instanceID].isActive ||
                    cursors[instanceID].isPlaying)
        ) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].cursor = ((cueFrameLength) * max(0.0, min(1.0, normal))).toFloat().toDouble()
    }

    /**
     * Returns a value indicating the current volume setting
     * of an `AudioCue` instance, ranging [0..1].
     *
     * @param instanceID - an `int` used to identify the
     * `AudioCue` instance
     * @return volume factor as a `double`
     * @throws IllegalStateException if instance is not active
     * @see .setVolume
     */
    fun getVolume(instanceID: Int): Double {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }
        return (if (cursors[instanceID].isPlaying) cursors[instanceID].volume else cursors[instanceID].newTargetVolume).toDouble()
    }

    /**
     * Sets the volume of the instance. Volumes can be altered
     * while an instance is either playing or stopped. When a
     * volume change is presented while the instance is playing, a
     * smoothing algorithm used to prevent signal discontinuities
     * that could result in audible clicks. If a second volume
     * change arrives before the first change is completed, the
     * most recent volume change takes precedence.
     *
     *
     * Arguments are clamped to the range [0..1], with 0 denoting
     * silence and 1 denoting the natural volume of the sample. In
     * other words, the volume control can only diminish the volume
     * of the media, not amplify it. Internally, the volume argument
     * is used as a factor that is directly multiplied against the
     * media's PCM values.
     *
     * @param  instanceID - an `int` used to identify the
     * `AudioCue` instance
     * @param  volume     - a `float` in the range [0, 1]
     * multiplied against the audio values
     * @throws IllegalStateException if instance is not active
     * @see .getVolume
     */
    fun setVolume(instanceID: Int, volume: Double) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].newTargetVolume = min(1.0, max(0.0, volume)).toFloat()
    }

    /**
     * Returns a double in the range [-1, 1] where -1 corresponds
     * to 100% left, 1 corresponds to 100% right, and 0
     * corresponds to center.
     *
     *
     * The calculations used to apply the pan are determined
     * by the `PanType`.
     *
     * @param instanceID - an `int` used to identify the
     * `AudioCue` instance
     * @return the current pan value, ranging [-1, 1]
     * @throws IllegalStateException if instance is not active
     * @see .setPan
     * @see PanType
     *
     * @see .setPanType
     */
    fun getPan(instanceID: Int): Double {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        return (if (cursors[instanceID].isPlaying) cursors[instanceID].pan else cursors[instanceID].newTargetPan).toDouble()
    }

    /**
     * Sets the pan of the instance, where 100% left
     * corresponds to -1, 100% right corresponds to 1, and
     * center = 0. The pan setting can either be changed
     * while a cue is either playing or stopped. If the instance
     * is playing, a smoothing algorithm used to prevent signal
     * discontinuities that result in audible clicks. If a second
     * pan change arrives before the first change is completed, the
     * most recent pan change takes precedence.
     *
     *
     * Arguments are clamped to the range [-1, 1]. The calculations
     * used to apply the pan are determined by the `PanType`.
     *
     * @param instanceID - an `int` used to identify the
     * `AudioCue` instance
     * @param pan        - a `double` ranging from -1 to 1
     * @throws IllegalStateException if instance is not active
     * @see .setPan
     * @see AudioCueFunctions.PanType
     *
     * @see .setPanType
     */
    fun setPan(instanceID: Int, pan: Double) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }
        cursors[instanceID].newTargetPan = min(1.0, max(-1.0, pan)).toFloat()
    }

    /**
     * Returns a factor indicating the current rate of play of
     * the `AudioCue` instance relative to normal play.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @return a `float` factor indicating the speed at
     * which the `AudioCue` instance is being played
     * in the range [0.125, 8]
     * @throws IllegalStateException if instance is not active
     */
    fun getSpeed(instanceID: Int): Double {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        return if (cursors[instanceID].isPlaying) cursors[instanceID].speed else cursors[instanceID].newTargetSpeed
    }

    /**
     * Sets the play speed of the `AudioCue` instance. A
     * faster speed results in both higher-pitched frequency
     * content and a shorter duration. Play speeds can be
     * altered in while a cue is either playing or stopped. If
     * the instance is playing, a smoothing algorithm used to
     * prevent signal discontinuities. If a second speed change
     * arrives before the first change is completed, the most
     * recent speed change takes precedence.
     *
     *
     * A speed of 1 will play the `AudioCue` instance at its
     * originally recorded speed. A value of 2 will double the
     * play speed, and a value of 0.5 will halve the play speed.
     * Arguments are clamped to values ranging from 8 times slower
     * to 8 times faster than unity, a range of [0.125, 8].
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @param speed      -a `double` factor ranging from
     * 0.125 to 8
     * @throws IllegalStateException if instance is not active
     */
    fun setSpeed(instanceID: Int, speed: Double) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].newTargetSpeed = min(8.0, max(0.125, speed))
    }

    /**
     * Sets the number of times the media will restart
     * from the beginning, after completing, or specifies
     * infinite looping via the value -1. Note: an instance
     * set to loop 2 times will play back a total of 3 times.
     *
     * @param instanceID  - an `int` used to identify an
     * `AudioCue` instance
     * @param loops       - an `int` that specifies the
     * number of times an instance will
     * return to the beginning and play
     * the instance anew
     * @throws IllegalStateException if instance is not active
     */
    fun setLooping(instanceID: Int, loops: Int) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].loop = loops
    }

    /**
     * Sets an internal flag which determines what happens when the
     * designated instance finishes playing. If `true` the
     * instance will be added to the pool of available instances and
     * will not accept updates. If `false` then the instance
     * will remain available for updates.
     *
     *
     * By default, an instance that is obtained and started via the
     * `play` method automatically recycles, and an instance
     * obtained via `getInstance` does not. In both cases the
     * behavior can be changed by setting this flag.
     *
     * @param instanceID      - an `int` used to identify an
     * `AudioCue` instance
     * @param recycleWhenDone - a `boolean` that designates
     * whether to recycle the instance or
     * not when the instance plays through
     * to completion
     * @throws IllegalStateException if the instance is not active
     */
    fun setRecycleWhenDone(instanceID: Int, recycleWhenDone: Boolean) {
        check(cursors[instanceID].isActive) {
            (name + " instance: "
                    + instanceID + " is inactive")
        }

        cursors[instanceID].recycleWhenDone = recycleWhenDone
    }

    /**
     * Returns `true` if the designated instance is active,
     * `false` if not. An active instance is one which is
     * not in the pool of available instances, but is open to
     * receiving commands. It may or may not be playing at any given
     * moment.
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @return `true` if the instance is active, `false`
     * if not
     * @see .getIsPlaying
     */
    fun getIsActive(instanceID: Int): Boolean {
        return cursors[instanceID].isActive
    }

    /**
     * Returns `true` if instance is playing, `false`
     * if not
     *
     * @param instanceID - an `int` used to identify an
     * `AudioCue` instance
     * @return `true` if instance is playing, `false`
     * if not
     */
    fun getIsPlaying(instanceID: Int): Boolean {
        return cursors[instanceID].isPlaying
    }


    /*
	 * A private, data-only class that is created and maintained
	 * internally for managing a single instance of an AudioCue.
	 * The immutable 'id' variable set during instantiation, with
	 * the value corresponding to the position of the AudioCueCursor
	 * instance in the AudioCue.cursors array.
	 *
	 * An instance is either active (isActive == true) in which case
	 * it can be updated, or inactive, in which case it is in a pool
	 * of <em>available</em> instances. An active instance can either
	 * be playing (isPlaying == true) or stopped (isPlaying == false).
	 *
	 * The 'recycleWhenDone' boolean is used to determine whether
	 * the instance is returned to the pool of available instances
	 * when a play completes, or if it is allowed to remain active
	 * and open to further commands.
	 *
	 * The target variables are used by operations that spread out
	 * changes over a preset number of steps (see VOLUME_STEPS,
	 * SPEED_STEPS, PAN_STEPS) to prevent discontinuities that
	 * could otherwise cause audible clicks.
	 */
    private inner class AudioCueCursor
        (val id: Int) {
        @Volatile
        var isPlaying: Boolean = false

        @Volatile
        var isActive: Boolean = false

        var cursor: Double = 0.0
        var speed: Double = 0.0
        var volume: Float = 0f
        var pan: Float = 0f
        var loop: Int = 0
        var recycleWhenDone: Boolean = false

        var newTargetSpeed: Double = 0.0
        var targetSpeed: Double = 0.0
        var targetSpeedIncr: Double = 0.0
        var targetSpeedSteps: Int = 0

        var newTargetVolume: Float = 0f
        var targetVolume: Float = 0f
        var targetVolumeIncr: Float = 0f
        var targetVolumeSteps: Int = 0

        var newTargetPan: Float = 0f
        var targetPan: Float = 0f
        var targetPanIncr: Float = 0f
        var targetPanSteps: Int = 0

        /*
          * Used to clear settings from previous plays
          * and put in default settings.
          */
        fun resetInstance() {
            isActive = false
            isPlaying = false
            cursor = 0.0

            volume = 0f
            newTargetVolume = 0f
            targetVolume = 0f
            targetVolumeSteps = 0

            pan = 0f
            newTargetPan = 0f
            targetPan = 0f
            targetPanSteps = 0

            speed = 1.0
            newTargetSpeed = 1.0
            targetSpeed = 1.0
            targetSpeedSteps = 0

            loop = 0
            recycleWhenDone = false
        }

        fun instantaneousUpdate() {
            check(isActive) {
                (name + " instance: "
                        + id + " is inactive")
            }
            check(!isPlaying) {
                (name + " instance: "
                        + id + " is playing")
            }


            // OK to execute instantaneous changes
            volume = newTargetVolume
            targetVolume = newTargetVolume
            targetVolumeSteps = 0

            pan = newTargetPan
            targetPan = newTargetPan
            targetPanSteps = 0

            speed = newTargetSpeed
            targetSpeed = newTargetSpeed
            targetSpeedSteps = 0
        }
    }

    /*
	 * "Opening" line sets the SourceDataLine waiting for data.
	 * "Run" will start loop that will either send out silence
	 * (zero-filled arrays) or sound data.
	 */
    private inner class AudioCuePlayer(mixer: Mixer?, bufferFrames: Int) : Runnable {
        private var sdl: SourceDataLine?
        private val sdlBufferSize: Int
        private var audioBytes: ByteArray

        //		private boolean playerRunning;
        fun stopRunning() {
            playerRunning = false
        }

        init {
            // twice the frames length, because stereo
            // NOTE: there is also a default instantiation
            // in the AudioCue constructor (to help with testing)
            readBuffer = FloatArray(bufferFrames * 2)
            // SourceDataLine must be 4 * number of frames, to
            // account for 16-bit encoding and stereo.
            sdlBufferSize = bufferFrames * 4
            audioBytes = ByteArray(sdlBufferSize)

            sdl = AudioCueFunctions.getSourceDataLine(mixer, info)
            sdl!!.open(audioFormat, sdlBufferSize)
            sdl!!.start()
        }

        // Audio Thread Code
        override fun run() {
            while (playerRunning) {
                readBuffer = fillBuffer(readBuffer)
                audioBytes = AudioCueFunctions.fromPcmToAudioBytes(audioBytes, readBuffer)
                sdl!!.write(audioBytes, 0, sdlBufferSize)
            }
            sdl!!.drain()
            sdl!!.close()
            sdl = null
        }
    }

    /*
	 * AudioThread code, executing within the while loop of the run() method.
	 */
    private fun fillBuffer(readBuffer: FloatArray): FloatArray {
        // Start with 0-filled buffer, send out silence
        // if nothing playing.
        val bufferLength = readBuffer.size
        for (i in 0..<bufferLength) {
            readBuffer[i] = 0f
        }

        for (ci in 0..<polyphony) {
            if (cursors[ci].isPlaying) {
                val acc = cursors[ci]
                /*
				 * Usually, these won't change, so initialize
				 * and store value and only recalculate upon change.
				 */
                var panFactorL: Float = panL!!.apply(acc.pan)!!
                var panFactorR: Float = panR!!.apply(acc.pan)!!
                var volFactor: Float = vol!!.apply(acc.volume)!!

                var i = 0
                while (i < bufferLength) {
                    // has volume setting changed? if so recalc
                    if (acc.newTargetVolume != acc.targetVolume) {
                        acc.targetVolume = acc.newTargetVolume
                        acc.targetVolumeIncr =
                            (acc.targetVolume - acc.volume) / VOLUME_STEPS
                        acc.targetVolumeSteps = VOLUME_STEPS
                    }
                    // adjust volume if needed
                    if (acc.targetVolumeSteps-- > 0) {
                        acc.volume += acc.targetVolumeIncr
                        if (acc.targetVolumeSteps == 0) {
                            acc.volume = acc.targetVolume
                        }
                        volFactor = vol!!.apply(acc.volume)!!
                    }


                    // has pan setting changed? if so, recalc
                    if (acc.newTargetPan != acc.targetPan) {
                        acc.targetPan = acc.newTargetPan
                        acc.targetPanIncr = (acc.targetPan - acc.pan) / PAN_STEPS
                        acc.targetPanSteps = PAN_STEPS
                    }
                    // adjust pan if needed
                    if (acc.targetPanSteps-- > 0) {
                        if (acc.targetPanSteps != 0) {
                            acc.pan += acc.targetPanIncr
                        } else {
                            acc.pan = acc.targetPan
                        }
                        panFactorL = panL!!.apply(acc.pan)!!
                        panFactorR = panR!!.apply(acc.pan)!!
                    }


                    // get audioVals, with LERP for fractional cursor position
                    var audioVals = FloatArray(2)
                    if (acc.cursor == acc.cursor.toInt().toDouble()) {
                        audioVals[0] = cue[acc.cursor.toInt() * 2]
                        audioVals[1] = cue[(acc.cursor.toInt() * 2) + 1]
                    } else {
                        audioVals = readFractionalFrame(audioVals, acc.cursor)
                    }

                    readBuffer[i] += ((audioVals[0]
                            * volFactor * panFactorL))
                    readBuffer[i + 1] += ((audioVals[1]
                            * volFactor * panFactorR))


                    // SET UP FOR NEXT ITERATION
                    // has speed setting changed? if so, recalc
                    if (acc.newTargetSpeed != acc.targetSpeed) {
                        acc.targetSpeed = acc.newTargetSpeed
                        acc.targetSpeedIncr =
                            (acc.targetSpeed - acc.speed) / SPEED_STEPS
                        acc.targetSpeedSteps = SPEED_STEPS
                    }
                    // adjust speed if needed
                    if (acc.targetSpeedSteps-- > 0) {
                        acc.speed += acc.targetSpeedIncr
                    }


                    // set NEXT read position
                    acc.cursor += acc.speed


                    // test for "eof" and "looping"
                    if (acc.cursor > (cueFrameLength - 1)) {
                        // keep looping indefinitely
                        if (acc.loop == -1) {
                            acc.cursor = 0.0
                            broadcastLoopEvent(acc)
                        } else if (acc.loop > 0) {
                            acc.loop--
                            acc.cursor = 0.0
                            broadcastLoopEvent(acc)
                        } else  // no more loops to do
                        {
                            acc.isPlaying = false
                            broadcastStopEvent(acc)
                            if (acc.recycleWhenDone) {
                                acc.resetInstance()
                                availables.offerFirst(acc)
                                broadcastReleaseEvent(acc)
                            }
                            // cursor is at end of cue before
                            // readBuffer filled, no need to
                            // process further (default 0's)
                            break
                        }
                    }
                    i += 2
                }
            }
        }
        return readBuffer
    }

    /*
	 *  Audio thread code, returns a single stereo PCM pair using a
	 *  LERP (linear interpolation) function. The difference between
	 *  `idx` (floating point value) and `intIndex` determines the
	 *  weighting amount for the LERP algorithm. As the PCM array of
	 *  audio data is stereo, we use `stereoIndex` (twice the amount
	 *  of `intIndex`) to locate the audio values to be weighted.
	 */
    private fun readFractionalFrame(audioVals: FloatArray, idx: Double): FloatArray {
        val intIndex = idx.toInt()
        val stereoIndex = intIndex * 2

        audioVals[0] = (cue[stereoIndex + 2] * (idx - intIndex)
                + cue[stereoIndex] * ((intIndex + 1) - idx)).toFloat()

        audioVals[1] = (cue[stereoIndex + 3] * (idx - intIndex)
                + cue[stereoIndex + 1] * ((intIndex + 1) - idx)).toFloat()

        return audioVals
    }

    // AudioMixerTrack interface
    override fun isTrackRunning(): Boolean {
        return trackRunning
    }

    // AudioMixerTrack interface
    override fun setTrackRunning(trackRunning: Boolean) {
        this.trackRunning = trackRunning
    }

    // AudioMixerTrack interface
    override fun readTrack(): FloatArray {
        return fillBuffer(readBuffer)
    }


    // Following are methods that broadcast events to registered listeners.
    private fun broadcastOpenEvent(
        threadPriority: Int,
        bufferSize: Int, name: String?
    ) {
        for (acl in listeners) {
            acl.audioCueOpened(
                System.currentTimeMillis(),
                threadPriority, bufferSize, this
            )
        }
    }

    private fun broadcastCloseEvent(name: String?) {
        for (acl in listeners) {
            acl.audioCueClosed(System.currentTimeMillis(), this)
        }
    }


    private fun broadcastCreateInstanceEvent(acc: AudioCueCursor) {
        for (acl in listeners) {
            acl.instanceEventOccurred(
                AudioCueInstanceEvent(
                    AudioCueInstanceEvent.Type.OBTAIN_INSTANCE,
                    this, acc.id, 0.0
                )
            )
        }
    }

    private fun broadcastReleaseEvent(acc: AudioCueCursor) {
        for (acl in listeners) {
            acl.instanceEventOccurred(
                AudioCueInstanceEvent(
                    AudioCueInstanceEvent.Type.RELEASE_INSTANCE,
                    this, acc.id, acc.cursor
                )
            )
        }
    }

    private fun broadcastStartEvent(acc: AudioCueCursor) {
        for (acl in listeners) {
            acl.instanceEventOccurred(
                AudioCueInstanceEvent(
                    AudioCueInstanceEvent.Type.START_INSTANCE,
                    this, acc.id, acc.cursor
                )
            )
        }
    }

    private fun broadcastLoopEvent(acc: AudioCueCursor) {
        for (acl in listeners) {
            acl.instanceEventOccurred(
                AudioCueInstanceEvent(
                    AudioCueInstanceEvent.Type.LOOP, this, acc.id, 0.0
                )
            )
        }
    }

    private fun broadcastStopEvent(acc: AudioCueCursor) {
        for (acl in listeners) {
            acl.instanceEventOccurred(
                AudioCueInstanceEvent(
                    AudioCueInstanceEvent.Type.STOP_INSTANCE,
                    this, acc.id, acc.cursor
                )
            )
        }
    }

    companion object {
        /**
         * A `javax.sound.sampled.AudioFormat`, set to the only
         * format used by `AudioCue`, also known as 'CD quality.'
         * The type is signed PCM, with a rate of 44100 frames per second,
         * with 16 bit encoding for each PCM value, stereo, and with the
         * constituent bytes of each PCM value given in little-endian order.
         */
        val audioFormat: AudioFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100f, 16, 2, 4, 44100f, false
        )

        /**
         * An immutable `javax.sound.sampled.Line.Info` that is used when obtaining a
         * `SourceDataLine` for media output.
         */
        val info: Line.Info = DataLine.Info(SourceDataLine::class.java, audioFormat)

        /**
         * A value indicating the default number of PCM frames in a buffer used in `AudioCuePlayer`
         * for processing media output.
         */
        const val DEFAULT_BUFFER_FRAMES: Int = 1024

        /**
         * A value indicating the number of frames over which the volume changes incrementally
         * when a new volume is given.
         */
        const val VOLUME_STEPS: Int = 1024

        /**
         * A value indicating the number of frames over which the speed setting changes incrementally
         * when a new speed value is given.
         */
        const val SPEED_STEPS: Int = 4096

        /**
         * A value indicating the number of frames over which the pan setting changes incrementally
         * when a new pan value is given.
         */
        const val PAN_STEPS: Int = 1024

        /**
         * Creates and returns a new `AudioCue`. This method
         * allows the direct insertion of a `float`
         * PCM array as an argument, where the data is presumed
         * to be stereo signed, normalized floats with a sample
         * rate of 44100 frames per second. The name for this
         * cue is set by the `name` argument.
         * The maximum number of concurrent playing instances
         * is set with the `polyphony` argument.
         *
         * @param cue - a `float` array of audio data
         * in "CD Quality" format, scaled to the range
         * [-1, 1]
         * @param name - a `String` to be associated
         * with the `AudioCue`
         * @param polyphony - an `int` specifying
         * the maximum number of concurrent instances
         * @return AudioCue
         */
        fun makeStereoCue(
            cue: FloatArray,
            name: String?, polyphony: Int
        ): AudioCue {
            return AudioCue(cue, name, polyphony)
        }

        /**
         * Creates and returns a new `AudioCue`. The file
         * designated by the `URL` argument is loaded and
         * held in memory. Only one format, known as "CD Quality",
         * is supported: 44100 frames per second, 16-bit encoding,
         * stereo, little-endian. The maximum number of concurrent
         * playing instances is given as the `polyphony` argument.
         * The file name is derived from the `URL` argument, but
         * can be changed via the method `setName`.
         *
         * @param url       - a `URL` for the source file
         * @param polyphony - an `int` specifying the maximum
         * number of concurrent instances
         * @return AudioCue
         * @throws UnsupportedAudioFileException if the media being loaded
         * is not 44100 fps, 16-bit, stereo, little-endian
         * @throws IOException if unable to load the file
         */
        @Throws(UnsupportedAudioFileException::class, IOException::class)
        fun makeStereoCue(url: URL, polyphony: Int): AudioCue {
            val urlName = url.getPath()
            val urlLen = urlName.length
            val name = urlName.substring(urlName.lastIndexOf("/") + 1, urlLen)
            val cue = loadURL(url)

            return AudioCue(cue, name, polyphony)
        }

        // Currently assumes stereo format ("CD Quality")
        @Throws(UnsupportedAudioFileException::class, IOException::class)
        private fun loadURL(url: URL): FloatArray {
            val ais = AudioSystem.getAudioInputStream(url)

            var framesCount = 0
            if (ais.getFrameLength() > Int.Companion.MAX_VALUE shr 1) {
                println(
                    "WARNING: Clip is too large to entirely fit!"
                )
                framesCount = Int.Companion.MAX_VALUE shr 1
            } else {
                framesCount = ais.getFrameLength().toInt()
            }


            // stereo output, so two entries per frame
            val temp = FloatArray(framesCount * 2)
            var tempCountdown = temp.size.toLong()

            var bytesRead = 0
            var bufferIdx: Int
            var clipIdx = 0
            val buffer = ByteArray(1024)
            while ((ais.read(buffer, 0, 1024).also { bytesRead = it }) != -1) {
                bufferIdx = 0
                var i = 0
                val n = (bytesRead shr 1)
                while (i < n) {
                    if (tempCountdown-- >= 0) {
                        temp[clipIdx++] =
                            ((buffer[bufferIdx++].toInt() and 0xff)
                                    or (buffer[bufferIdx++].toInt() shl 8)).toFloat()
                    }
                    i++
                }
            }
            // Done with AudioInputStream
            ais.close()

            for (i in temp.indices) {
                temp[i] = temp[i] / 32767f
            }

            return temp
        }
    }
}