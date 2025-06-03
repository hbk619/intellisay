package com.hbk619.intellisay.audiocue

/**
 * Represents an event in the life cycle of an `AudioCue` play
 * back instance and is passed as an argument to objects that implement
 * the `AudioCueListener` interface and are registered to listen.
 *
 *
 * `AudioCue` supports concurrent media writes by managing
 * a pool of instances. The lifecycle of an instance is as
 * follows:
 *
 *  * OBTAIN_INSTANCE: an instance is obtained from the pool
 * of available instances if the limit of concurrent instances
 * is not exceeded
 *  * START_INSTANCE: an instance starts to play
 *  * LOOP: a instance that finishes playing restarts from the
 * beginning
 *  * STOP_INSTANCE: a playing instance is stopped (but can
 * be restarted)
 *  * RELEASE_INSTANCE: an instance is released back into the
 * pool of available instances.
 *
 *
 *
 * An `AudioCueInstanceEvent` holds following immutable fields:
 *
 *  * **type** - an `enum`,
 * `AudioCueInstanceEvent.Type` designating the event
 *  * **time** - a `long` containing the time
 * of occurrence of the event, to the nearest millisecond
 *  * **source** - the parent `AudioCue`
 *  * **frame** - a `double` containing the frame
 * (may be a fractional value) that was current at the time of the event
 *  * **instanceID** - an `int` used to identify the
 * `AudioCue` play back instance.
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 *
 * @see AudioCueListener
 */
class AudioCueInstanceEvent(
    /**
     * the triggering event
     */
    val type: Type?,
    /**
     * the `AudioCue` from which the event originated
     */
    val source: AudioCue?,
    /**
     * the identifier for the parent AudioCue
     */
    val instanceID: Int,
    /**
     * the sample frame number (may be fractional) current
     * at the time of the event
     */
    val frame: Double
) {
    /**
     * An enumeration of events that occur during the lifetime of an
     * `AudioCue` instance.
     */
    enum class Type {
        /**
         * Indicates that an instance has been obtained from
         * the pool of available instances.
         */
        OBTAIN_INSTANCE,

        /**
         * Indicates that an instance has been released and
         * returned to the pool of available instances.
         */
        RELEASE_INSTANCE,

        /**
         * Indicates that an instance has started playing.
         */
        START_INSTANCE,

        /**
         * Indicates that an instance has stopped playing.
         */
        STOP_INSTANCE,

        /**
         * Indicates that an instance has finished playing
         * and is starting to play again from the beginning
         * of the media.
         */
        LOOP
    }

    /**
     * the time in milliseconds when the event occurred
     */
    val time: Long

    /**
     * Constructor for `AudioCueInstanceEvent`, creating an
     * instance of a data-holding class to be passed as a parameter
     * for the `AudioCuelistener` method `instanceEventOccurred`
     *
     * @param type       - an `enum` that designates type of event
     * @param source     - the parent `AudioCue`
     * @param instanceID - an `int` identifier for the parent
     * `AudioCue` instance
     * @param frame      - a `double` that holds the sample frame
     * current at the time of the event
     */
    init {
        time = System.currentTimeMillis()
    }
}