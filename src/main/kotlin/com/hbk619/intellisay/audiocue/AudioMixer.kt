package com.hbk619.intellisay.audiocue

import com.hbk619.intellisay.audiocue.AudioCueFunctions.fromPcmToAudioBytes
import com.hbk619.intellisay.audiocue.AudioCueFunctions.getSourceDataLine
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.Volatile

/**
 * An `AudioMixer` mixes the media content of all the members
 * of a `AudioMixerTrack` collection into a single output
 * line. Classes implementing `AudioMixerTrack` can be added
 * and removed from the mix asynchronously, with the operation
 * occurring at the next iteration of the read buffer. Unlike a
 * mixer used in sound studios, the `AudioMixer` does not
 * provide functions such as panning or volume controls.
 *
 *
 * An `SourceDataLine` can be in one of two states: (1) running,
 * or (2) not running. When running, audio data is read from the
 * constituent tracks, mixed and written as a single stream using a
 * `javax.sound.sampled.SourceDataLine`. The mixer imposes a
 * simple floor/ceiling of -1, 1, to guard against volume overflows.
 * When not running, the `SourceDataLine` is allowed to drain
 * and close. A new `SourceDataLine` is instantiated if/when
 * this `AudioMixer` is reopened.
 *
 *
 * Values used to configure the media output are provided in the
 * constructor, and are held as immutable. These include a
 * `javax.sound.sampled.Mixer` used to provide the
 * `SourceDataLine`, the size of the buffer used for iterative
 * reads of the PCM data, and the thread priority. Multiple constructors
 * are provided to facilitate the use of default values. These
 * configuration values override those associated with the constituent
 * tracks.
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 *
 * @see AudioMixerTrack
 */
class AudioMixer @JvmOverloads constructor(
    private val mixer: Mixer? = null,
    /**
     * An immutable number of PCM frames held in array buffers, set
     * during instantiation.
     */
    val bufferFrames: Int = 1024 * 8,
    /**
     * A value that holds the priority level of the thread that that handles
     * the media output, set upon instantiation of the class. The value is
     * clamped to the range `java.lang.Thread.MIN_PRIORITY` to
     * `java.lang.Thread.MAX_PRIORITY`.
     */
    val threadPriority: Int = Thread.MAX_PRIORITY
) : AutoCloseable {
    private var trackCache: ArrayList<AudioMixerTrack> = arrayListOf()

    private val trackManager: CopyOnWriteArrayList<AudioMixerTrack> = CopyOnWriteArrayList<AudioMixerTrack>()

    @Volatile
    private var trackCacheUpdated = false

    /**
     * Returns the number of tracks being mixed.
     *
     * @return integer number of tracks being mixed.
     */
    var tracksCount: Int = 0
        private set

    /**
     * An immutable number describing the size of an internal array
     * buffer, set during instantiation. The `readBufferSize` has
     * two PCM values per each frame being handled, corresponding to the
     * left and right stereo channels, and is calculated by multiplying
     * `bufferFrames` by 2.
     */
    val readBufferSize: Int

    /**
     * An immutable number describing the size of an internal array
     * buffer, set during instantiation. The `sdlBufferSize` has
     * four bytes per frame, as each of the two PCM values per frame is
     * encoded into two constituent bytes. The `sdlByteBufferSize`
     * is calculated by multiplying `bufferFrames` by 4.
     */
    val sdlByteBufferSize: Int

    @Volatile
    private var mixerRunning = false

    /**
     * Constructor for `AudioMixer`. The buffer size
     * is the number of frames collected in a single `while`
     * loop iteration. A buffer with a corresponding frame
     * count, calculated in bytes, is assigned to the
     * `SourceDataLine`. A thread priority of 10 is
     * recommended, in order to help prevent sound drop outs.
     * Note that a well designed and properly running sound thread
     * should spend the vast majority of its time in a blocked
     * state, and thus have a minimal impact in terms of usurping
     * cpu cycles from other threads.
     *
     * @param mixer javax.sound.sampled.Mixer to be used
     * @param bufferFrames int specifying the number of frames to
     * process with each iteration
     * @param threadPriority int ranging from 1 to 10 specifying
     * the priority of the sound thread
     */
    /**
     * Constructor for `AudioMixer`, using default
     * settings:
     * Mixer           = system default
     * Buffer size     = 8192 frames
     * Thread priority = 10.
     *
     * The buffer size pertains to the frames collected in
     * a single `while` loop iteration. A buffer that
     * corresponds to the same number of frames converted
     * to bytes is assigned to the `SourceDataLine`.
     */
    init {
        this.readBufferSize = bufferFrames * 2
        this.sdlByteBufferSize = bufferFrames * 4
    }

    // reminder: this does NOT update the trackCache!!
    /**
     * Designates an `AudioMixerTrack` to be staged for addition
     * into the collection of tracks actively being mixed. If the
     * `AudioMixer` is running, actual addition occurs when the
     * `updateTracks` method is executed. If the
     * `AudioMixer` is not running, the addition will occur
     * automatically when the `start` method is called.
     *
     * @param track - an `AudioMixerTrack` to be added to the mix
     * @see .removeTrack
     * @see .updateTracks
     */
    fun addTrack(track: AudioMixerTrack?) {
        trackManager.add(track)
    }

    // reminder: this does NOT update the trackCache!!
    /**
     * Designates an `AudioMixerTrack` to be staged for removal
     * from the collection of tracks actively being mixed.  If the
     * `AudioMixer` is running, actual removal occurs when the
     * `updateTracks` method is executed.  If the
     * `AudioMixer` is not running, the removal will occur
     * automatically when the `start` method is called.
     *
     * @param track - an `AudioMixerTrack` to be removed from the mix
     * @see .addTrack
     * @see .updateTracks
     */
    fun removeTrack(track: AudioMixerTrack?) {
        trackManager.remove(track)
    }

    /**
     * Signals the internal media writer to load an updated
     * `AudiomixerTrack` collection at the next opportunity.
     * Tracks to be added or removed are first staged using the
     * methods `addTrack` and `removeTrack`.
     * @see .addTrack
     * @see .removeTrack
     */
    fun updateTracks() {
        val size = trackManager.size
        val workCopyTracks = arrayListOf<AudioMixerTrack>()
        for (i in 0..<size) {
            workCopyTracks.add(trackManager.get(i))
        }

        trackCache = workCopyTracks
        trackCacheUpdated = true
    }

    /**
     * Starts the operation of the `AudioMixer`. A running
     * `AudioMixer` iteratively sums a buffer's worth of
     * frames of sound data from a collection of
     * `AudioMixerTrack`s, and writes the resulting
     * array to a `SourceDataLine`.
     *
     * @throws IllegalStateException is thrown if the
     * `AudioMixer` is already running.
     * @throws LineUnavailableException is thrown if there
     * is a problem securing a `SourceDataLine`
     */
    @Throws(LineUnavailableException::class)
    fun start() {
        check(!mixerRunning) { "AudioMixer is already running!" }

        updateTracks()

        val player = AudioMixerPlayer(mixer)
        val t = Thread(player)
        t.setPriority(threadPriority)
        t.start()

        mixerRunning = true
    }

    /**
     * Sets a flag that will signal the `AudioMixer` to stop
     * media writes and release resources.
     *
     * @throws IllegalStateException if the `AudioMixer`
     * is already in a stopped state.
     */
    fun stop() {
        check(mixerRunning) { "PFCoreMixer already stopped!" }

        mixerRunning = false
    }

    @Throws(Exception::class)
    override fun close() {
        stop()
    }

    private inner class AudioMixerPlayer(mixer: Mixer?) : Runnable {
        private var sdl: SourceDataLine?
        private var readBuffer: FloatArray
        private var audioBytes: ByteArray
        private var mixerTracks: ArrayList<AudioMixerTrack> = arrayListOf()
        private var audioData: FloatArray = FloatArray(0)

        init {
            audioBytes = ByteArray(sdlByteBufferSize)
            readBuffer = FloatArray(readBufferSize)

            sdl = getSourceDataLine(mixer, AudioCue.info)
            sdl!!.open(AudioCue.audioFormat, sdlByteBufferSize)
            sdl!!.start()
        }

        // Sound Thread
        override fun run() {
            while (mixerRunning) {
                if (trackCacheUpdated) {
                    /*
		    		 * Concurrency plan: Better to allow a late
		    		 * or redundant update than to skip an update.
		    		 */
                    trackCacheUpdated = false
                    mixerTracks = trackCache
                    tracksCount = mixerTracks.size
                }
                Arrays.fill(readBuffer, 0f)
                readBuffer = fillBufferFromTracks(readBuffer)
                audioBytes = fromPcmToAudioBytes(audioBytes, readBuffer)
                sdl!!.write(audioBytes, 0, sdlByteBufferSize)
            }

            sdl!!.drain()
            sdl!!.close()
            sdl = null
        }

        fun fillBufferFromTracks(normalizedOut: FloatArray): FloatArray {
            // loop through all tracks, summing
            for (n in 0..<tracksCount) {
                if (mixerTracks[n]!!.isTrackRunning()) {
                    try {
                        audioData = mixerTracks[n]!!.readTrack()!!
                        for (i in 0..<readBufferSize) {
                            normalizedOut[i] += audioData[i]
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                for (i in 0..<readBufferSize) {
                    if (normalizedOut[i] > 1) {
                        normalizedOut[i] = 1f
                    } else if (normalizedOut[i] < -1) {
                        normalizedOut[i] = -1f
                    }
                }
            }
            return normalizedOut
        }
    }
}