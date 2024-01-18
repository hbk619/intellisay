package com.hbk619.jetbrainsscreenreader.settings

import com.hbk619.jetbrainsscreenreader.sound.Say
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue

class ErrorBeep : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Playing sound")
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.errorsOn = !settingsState.errorsOn
        val speech = Say(event.project, "Error settings", "Errors are ${if (settingsState.errorsOn) "on" else "off"}")
        queue.run(speech)
    }
}