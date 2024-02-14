package com.hbk619.jetbrainsscreenreader.settings

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue

class ErrorBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.errorsOn = !settingsState.errorsOn
        sayText(event.project, "Error settings", "Errors are ${if (settingsState.errorsOn) "on" else "off"}")
    }
}