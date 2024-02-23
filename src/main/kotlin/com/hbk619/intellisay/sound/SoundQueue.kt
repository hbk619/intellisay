package com.hbk619.intellisay.sound

import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project

const val soundQueueTitle = "IntelliSay sound"
class SoundQueue: PlayerQueue, BackgroundTaskQueue(null, soundQueueTitle)  {
    override fun play(project: Project?, title: String, sound: Sound) {
        val soundTask = SoundTask(project, title, sound)

        run(soundTask)
    }
}