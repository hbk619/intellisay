package com.hbk619.intellisay.audiocue

import java.util.function.Function
import javax.sound.sampled.*
import kotlin.math.*

/**
 * A class containing static functions and public
 * enums used in the AudioCue package
 *
 * Original Source https://github.com/philfrei/AudioCue-maven/tree/v2.1.0
 */
object AudioCueFunctions {
    /**
     * Obtains a `SourceDataLine` that is available for use from the
     * specified `javax.sound.sampled.Mixer` and that matches the
     * description in the specified `Line.Info`.
     *
     * @param mixer - an `javax.sound.sampled.Mixer`
     * @param info - describes the desired line
     * @return a a line that is available for use from the specified
     * `javax.sound.sampled.Mixer` and that matches the
     * description	in the specified `Line.Info` object
     * @throws LineUnavailableException if a matching line is not available
     */
    @Throws(LineUnavailableException::class)
    fun getSourceDataLine(
        mixer: Mixer?,
        info: Line.Info?
    ): SourceDataLine? {
        val sdl: SourceDataLine?

        if (mixer == null) {
            sdl = AudioSystem.getLine(info) as SourceDataLine?
        } else {
            sdl = mixer.getLine(info) as SourceDataLine?
        }

        return sdl
    }

    /**
     * Converts an array of signed, normalized float PCM values to a
     * corresponding byte array using 16-bit, little-endian encoding.
     * This is the sole audio format supported by this application,
     * and is expected by the `SourceDataLine` configured for
     * media play. Because each float value is converted into two
     * bytes, the receiving array, `audioBytes`, must be twice
     * the length of the array of data to be converted, `sourcePcm`.
     * Failure to comply will throw an `IllegalArgumentException`.
     *
     * @param audioBytes - an byte array ready to receive the converted
     * audio data.	Should be twice the length of
     * `buffer`.
     * @param sourcePcm - a float array with signed, normalized PCM data to
     * be converted
     * @return the byte array `audioBytes` after is has been populated
     * with the converted data
     * @throws IllegalArgumentException if destination array is not exactly
     * twice the length of the source array
     */
    fun fromPcmToAudioBytes(audioBytes: ByteArray, sourcePcm: FloatArray): ByteArray {
        require(sourcePcm.size * 2 == audioBytes.size) { "Destination array must be exactly twice the length of the source array" }

        var i = 0
        val n = sourcePcm.size
        while (i < n) {
            sourcePcm[i] *= 32767f

            audioBytes[i * 2] = sourcePcm[i].toInt().toByte()
            audioBytes[i * 2 + 1] = (sourcePcm[i].toInt() shr 8).toByte()
            i++
        }

        return audioBytes
    }

    /**
     * The `enum VolumeType` is a repository of functions
     * used to convert an input in the linear range 0..1 to an
     * attenuation factor, where the input 0 indicating silence
     * and 1 indicating full volume, and the returned factor
     * intended to be multiplied to the PCM values on a per-element
     * basis.
     *
     *
     * The perception of amplitudes is not linear, but exponential,
     * and commonly measured using the deciBel unit. The formula
     * x^4 is widely used as a "good enough" approximation of the
     * of the more costly dB calculation: exp(x * 6.908)/1000 that
     * spans a range of 60 dB.
     *
     *
     * A straight use of linear values will tend to result in
     * hard-to-perceive changes in the upper range and more extreme
     * sensitivity with the lower values. But with a 60dB dynamic range,
     * the x^4 approximation may have the opposite problem, with
     * values below 0.5 quickly becoming inaudible. For this reason,
     * a selection of intermediate exponential curves are offered.
     *
     * @version 2.1.0
     * @since 2.1.0
     * @author Philip Freihofner
     *
     * @see AudioCue.setVolType
     */
    enum class VolType
        (vol: Function<Float?, Float?>) {
        /**
         * Represents an amplitude function that directly uses linear
         * linear values ranging from 0 (silent) to 1 (maximum amplitude).
         * This volume control tends to result in most of the
         * amplification occurring in the lower end of the numerical range.
         * @see VolType
         */
        LINEAR(Function { x: Float? -> x }),

        /**
         * Represents an amplitude function that tends to result in most
         * of the perceived amplification taking place in the lower numerical
         * range, but less so than the LINEAR function. Input values, ranging
         * from 0 (silent) to 1 (full volume) are mapped to volume factors
         * with the function **f(x) = x * x**.
         * @see VolType
         */
        EXP_X2(Function { x: Float? -> x as Float * x }),

        /**
         * Represents an 'intermediate' amplitude function that is somewhat
         * louder in the lower numeric range than EXP_X4, but quieter in
         * the lower numeric range than EXP_X2. Input values, ranging
         * from **0** (*silent*) to **1**
         * (*full volume*) are mapped to volume factors with the
         * function **f(x) = x * x * x**.
         * @see VolType
         */
        EXP_X3(Function { x: Float? -> x as Float * x * x }),

        /**
         * Represents an amplitude function that is commonly used to map
         * the linear values ranging from **0** (*silent*)
         * to **1** (*full volume*) to volume factors
         * that approximate the geometric curve of human hearing. With a
         * dynamic range of 60 dB, values below 0.5 can quickly become
         * inaudible. For this reason, functions with louder lower ends
         * (EXP_X3, EXP_X2) are also offered. The mapping function is
         * calculated as follows: **f(x) = x * x * x * x**
         * @see VolType
         */
        EXP_X4(Function { x: Float? -> x as Float * x * x * x }),

        /**
         * Represents an amplitude function with enhanced sensitivity to
         * volume changes occurring in the upper numerical area between
         * **0** (*silent*) and **1**
         * (*full volume*) The mapping function is
         * calculated as follows: **f(x) = x * x * x * x * x**
         * @see VolType
         */
        EXP_X5(Function { x: Float? -> x as Float * x * x * x * x }),

        /**
         * Represents an amplitude function that best maps to the geometric
         * curve of human hearing, but at a higher computational cost than
         * EXP_X4. The mapping function is calculated as follows:<br></br>
         * **f(x) = Math.exp(x * 6.908) / 1000.0**<br></br>
         * The input value of 0 is directly mapped to zero instead of using
         * the above function.
         * @see VolType
         */
        EXP_60dB(Function { x: Float? -> if (x == 0f) 0f else (exp(x!! * 6.908) / 1000.0).toFloat() });

        val vol: Function<Float?, Float?>?

        init {
            this.vol = vol
        }
    }

    /**
     * The `enum PanType` is a repository of functions
     * for volume-based panning for stereo media.Each function
     * takes a linear pan setting as an input, ranging
     * from -1 (100% left) to 1 (100% right) with 0 being the
     * center pan setting.
     *
     * @version 2.1.0
     * @since 2.0.0
     * @author Philip Freihofner
     *
     * @see PanType.FULL_LINEAR
     *
     * @see PanType.LEFT_RIGHT_CUT_LINEAR
     *
     * @see PanType.SQUARE_LAW
     *
     * @see PanType.SINE_LAW
     *
     * @see AudioCue.setPanType
     */
    enum class PanType
        (
        left: Function<Float?, Float?>,
        right: Function<Float?, Float?>
    ) {
        /**
         * Represents a panning function that uses linear
         * gradients that taper from edge to edge, and the
         * combined volume is stronger at the edges than
         * at the center. For pan values -1 to 1 the left
         * channel factor is tapered with a linear function
         * from 1 to 0, and the right channel factor is
         * tapered via a linear function from 0 to 1.
         * @see PanType
         */
        FULL_LINEAR(
            Function { x: Float? -> 1 - ((1 + x!!) / 2) },
            Function { x: Float? -> (1 + x!!) / 2 }
        ),

        /**
         * Represents a panning function that uses linear
         * gradients that taper from the center to the edges
         * on the weak side, and the combined volume is
         * stronger at the center than at the edges.
         * For the pan values -1 to 0, the
         * left channel factor is kept at full volume ( = 1)
         * and the right channel factor is tapered via a
         * linear function from 0 to 1.
         * For pan values from 0 to 1, the left channel factor
         * is tapered via a linear function from 0 to 1 and the
         * right channel is kept at full volume ( = 1).
         * @see PanType
         */
        LEFT_RIGHT_CUT_LINEAR(
            Function { x: Float? -> max(0f, min(1f, 1 - x!!)) },
            Function { x: Float? -> max(0f, min(1f, 1 + x!!)) }
        ),

        /**
         * Represents a panning function that uses square
         * roots to taper the amplitude from edge to edge,
         * while maintaining the same total power of the
         * combined tracks across the panning range.
         *
         *
         * For inputs -1 (full left) to 1 (full right):<br></br>
         * Left vol factor = Math.sqrt(1 - (1 + x) / 2.0) <br></br>
         * Right vol factor = Math.sqrt((1 + x) / 2.0)
         *
         *
         * Settings will tend to sound a little more central
         * than with the use of SINE_LAW panning.
         * @see PanType
         *
         * @see PanType.SINE_LAW
         */
        SQUARE_LAW(
            Function { x: Float? -> (sqrt(1 - (1 + x!!) / 2.0)).toFloat() },
            Function { x: Float? -> (sqrt((1 + x!!) / 2.0)).toFloat() }
        ),

        /**
         * Represents a panning function that uses sines
         * to taper the amplitude from edge to edge while
         * maintaining the same total power of the combined
         * tracks across the panning range.
         *
         *
         * For inputs -1 (full left) to 1 (full right):<br></br>
         * Left vol factor = Math.sin((Math.PI / 2 ) * (1 - (1 + x) / 2.0)) <br></br>
         * Right vol factor = Math.sin((Math.PI / 2 ) * ((1 + x) / 2.0))
         *
         *
         * Settings will tend to sound a little more spread
         * than with the use of SQUARE_LAW panning.
         *
         * @see PanType
         *
         * @see PanType.SQUARE_LAW
         */
        SINE_LAW(
            Function { x: Float? -> (sin((Math.PI / 2) * (1 - (1 + x!!) / 2.0))).toFloat() },
            Function { x: Float? -> (sin((Math.PI / 2) * ((1 + x!!) / 2.0))).toFloat() }
        );

        val left: Function<Float?, Float?>?
        val right: Function<Float?, Float?>?

        init {
            this.left = left
            this.right = right
        }
    }
}