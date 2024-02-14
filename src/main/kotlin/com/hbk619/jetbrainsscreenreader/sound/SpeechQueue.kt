package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project

const val title = "IntelliSay speaking"
class SpeechQueue : AudibleQueue, BackgroundTaskQueue(null, title) {
    override fun say(project: Project, value: String, optionalTitle: String?) {
        val speech = sayText(project, optionalTitle ?: title, value)

        run(speech)
    }

    override fun say(value: String, optionalTitle: String?) {
        val speech = sayText(null, optionalTitle ?: title, value)

        run(speech)
    }

    override fun say(value: String) {
        val speech = sayText(null, title, value)

        run(speech)
    }
}