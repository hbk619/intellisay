package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

enum class Sound(val fileName: String) {
    ERROR("error.wav"),
    WARNING("warning.wav"),
    BREAKPOINT_ADDED("breakpoint-added.wav"),
    BREAKPOINT_REMOVED("breakpoint-removed.wav"),
    BREAKPOINT("breakpoint.wav")
}

class Player(project: Project?, title: String, private val sound: Sound) : Task.Backgroundable(project, title) {
    private val log = Logger.getInstance(Player::class.java)

    override fun run(progressIndicator: ProgressIndicator) {
        try {
            val path = Player::class.java.getResource(sound.fileName)
            val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(path)
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
            while (clip.framePosition<clip.frameLength) {
                log.debug("Running sound $sound")
            }
        } catch (e: Exception) {
            log.error(e)
        }
    }
}