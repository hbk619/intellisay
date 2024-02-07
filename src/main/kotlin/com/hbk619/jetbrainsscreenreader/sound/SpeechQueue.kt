package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project

class SpeechQueue(private val project: Project?, private val title: String) : AudibleQueue, BackgroundTaskQueue(project, title) {
    override fun say(value: String, optionalTitle: String?) {
        val s = Say(project, optionalTitle ?: title, value)

        run(s)
    }

    override fun say(value: String) {
        val s = Say(project, title, value)

        run(s)
    }
}