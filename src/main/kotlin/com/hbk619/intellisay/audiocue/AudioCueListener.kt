package com.hbk619.intellisay.audiocue

/**
 * A listener interface for receiving notifications of events
 * pertaining to an `AudioCue` and to its individual play
 * back instances.
 *
 *
 * The execution of the implemention method `instanceEventOccurred`
 * may occur on the same thread that processes the audio data (for example,
 * the `AudioCueInstanceEvent` type `LOOP`), and thus should
 * be coded for brevity in order to minimize non-audio processing that
 * could potentially contribute to latency during media play.
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 *
 * @see .audioCueOpened
 * @see .audioCueClosed
 * @see .instanceEventOccurred
 * @see AudioCueInstanceEvent
 */
interface AudioCueListener {
    /**
     * Method called when an `AudioCue` executes its
     * `open` method.
     *
     * @param now            - a `long` holding millisecond value
     * @param threadPriority - an `int` specifying thread
     * priority
     * @param bufferSize     - and `int` specifying buffer size
     * in frames
     * @param source         - the parent `AudioCue` that originated
     * the notification
     */
    fun audioCueOpened(
        now: Long, threadPriority: Int, bufferSize: Int,
        source: AudioCue?
    )

    /**
     * Method called when an `AudioCue` executes its
     * `close` method.
     * S
     * @param now    - a `long` holding a millisecond value
     * @param source - the parent `AudioCue` that originated the
     * notification
     */
    fun audioCueClosed(now: Long, source: AudioCue?)

    /**
     * Method called when an `AudioCue` instance event
     * occurs.
     *
     * @param event -an `AudioCueInstanceEvent`
     *
     * @see AudioCueInstanceEvent
     */
    fun instanceEventOccurred(event: AudioCueInstanceEvent?)
}