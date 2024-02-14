package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project

class SpeechQueue(private val project: Project?, private val title: String) : AudibleQueue, BackgroundTaskQueue(project, title) {
    override fun say(value: String, optionalTitle: String?) {
        val speech = say(project, optionalTitle ?: title, value)

        run(speech)

    override fun say(value: String, optionalTitle: String?) {
        val speech = sayText(null, optionalTitle ?: title, value)

        run(speech)
    }

    override fun say(value: String) {
        val speech = sayText(null, title, value)

        run(speech)
    }
}