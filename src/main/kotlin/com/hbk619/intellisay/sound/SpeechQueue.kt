package com.hbk619.intellisay.sound

import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project
import java.nio.charset.Charset

const val title = "IntelliSay speaking"
class SpeechQueue : AudibleQueue, BackgroundTaskQueue(null, title) {
    override fun say(project: Project, value: String, optionalTitle: String?) {
        val speech = createSpeech(project, optionalTitle ?: title, value)

        run(speech)
    }

    override fun say(value: String, optionalTitle: String?) {
        val speech = createSpeech(null, optionalTitle ?: title, value)

        run(speech)
    }

    override fun say(value: String) {
        val speech = createSpeech(null, title, value)

        run(speech)
    }

    private fun createSpeech(project: Project?, title: String, text: String): SpeechTask {
        val commands = getSpeechCommand(text)
        val command = GeneralCommandLine(commands)
        command.charset = Charset.forName("UTF-8")
        command.setWorkDirectory(project?.basePath ?: ".")

        return SpeechTask(project, title, command)
    }

    private fun getSpeechCommand(text: String): List<String> {
        if (AppSettingsState.instance.useVoiceOver) {
            val applescript = "tell application \"VoiceOver\"\n" +
                    "\toutput \"$text\"\n" +
                    "end tell"
            return listOf("/usr/bin/osascript", "-e", applescript)
        } else {
            return listOf("say", text)
        }
    }
}