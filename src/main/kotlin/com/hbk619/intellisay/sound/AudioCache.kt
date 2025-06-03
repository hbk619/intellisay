package com.hbk619.intellisay.sound

import com.hbk619.intellisay.audiocue.AudioCue
import java.net.URL

object AudioCache {
    private val clips = mutableMapOf<String, AudioCue>()

    fun getClip(soundPath: URL): AudioCue {
        return clips.getOrPut(soundPath.toString()) {
            val myAudioCue = AudioCue.makeStereoCue(soundPath, 4);
            myAudioCue.open();
            myAudioCue
        }
    }
}
