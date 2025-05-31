package com.hbk619.intellisay.sound

import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

enum class Sound(val fileName: String) {
    ERROR("error.wav"),
    COMPILE_ERROR("compile_error.wav"),
    WARNING("warning.wav"),
    COMPILE_WARNING("compile_warning.wav"),
    BREAKPOINT_ADDED("breakpoint-added.wav"),
    BREAKPOINT_REMOVED("breakpoint-removed.wav"),
    BREAKPOINT("breakpoint.wav")
}

fun playSound(project: Project?, title: String, sound: Sound) {
    val aQueue: PlayerQueue = ApplicationManager.getApplication().getService(PlayerQueue::class.java)
    aQueue.play(project, title, sound)
}

class SoundTask(project: Project?, title: String, private val sound: Sound) : Task.Backgroundable(project, title) {
    private val log = Logger.getInstance(PlayerQueue::class.java)
    override fun run(progressIndicator: ProgressIndicator) {
        try {
            val pathToSound = getSoundFileURL()
            val clip = AudioCache.getClip(pathToSound)
            val id: Int = clip.obtainInstance()
            clip.setVolume(id, 1.0)
            clip.setRecycleWhenDone(id, true)
            clip.start(id)

            while (clip.getIsPlaying(id)) {
                log.debug("Running sound $sound")
            }

            clip.releaseInstance(id);
        } catch (e: Exception) {
            log.error(e)
        }
    }

    private fun getSoundFileURL(): URL {
        val customFolder = AppSettingsState.instance.soundsLocation
        val customPathToSound = Path.of(customFolder, sound.fileName)

        var pathToSound = PlayerQueue::class.java.getResource(sound.fileName)!!
        if (customFolder.isNotEmpty() && Files.exists(customPathToSound)) {
            pathToSound = URI.create("file:${customPathToSound}").toURL()
            log.debug("Using custom sound ${pathToSound?.path}")
        }
        return pathToSound
    }
}