package com.hbk619.intellisay.audiocue

import java.io.IOException

/**
 * An interface for classes that make audio data available to an
 * `AudioMixer` for media play via the `read` method.
 *
 *
 * Objects that implement `AudioMixerTrack` can either be (1) running,
 * or (2) not running. There are no explicit restrictions related to the
 * relationship between the state and the read method. Nor are there any
 * restrictions or promises made as to the content of the returned float
 * array.
 *
 *
 * However, in this package, the following conditions have been implemented:
 *  * The `read` method will only be executed after the track is
 * first shown to be in a running state;
 *  * the read array is expected to consist of signed, normalized floats of
 * stereo PCM encoded at 44100 frames
 * per second.
 *
 *
 * With these constraints in place, the media write of audio from the track
 * can in effect be muted by setting the state to not running, and the media
 * writes can be set to resume by setting the state to running.
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 *
 * @see AudioMixer
 */
interface AudioMixerTrack {
    /**
     * Indicates if the track is or is not being included in the
     * `AudioMixer` media out. If the method returns `true`,
     * this track is included in the mix. If `false`, the track
     * is ignored, as if a 'mute' button had been pressed.
     *
     * @return `true` if the track is being included in the mix,
     * otherwise `false`
     */
    fun isTrackRunning(): Boolean

    /**
     * Used to set whether or not this `AudioMixerTrack` is to be
     * included in the `AudioMixer` media out. When set to
     * `true`, this `AudioMixerTrack` will be included in
     * the audio mix. When set to `false`, this track will be ignored,
     * and not included in the audio mix, as if a 'mute' button had
     * been pressed.
     *
     * @param bool - if `true`, this track will be included in the
     * audio mix, if `false` this track will not be
     * included
     */
    fun setTrackRunning(trackRunning: Boolean)

    /**
     * Reads one buffer of normalized audio data frames of
     * the track.
     * @return one buffer of normalized audio frames
     * @throws IOException if an I/O exception occurs
     */
    @Throws(IOException::class)
    fun readTrack(): FloatArray?
}