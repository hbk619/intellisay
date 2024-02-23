package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ErrorBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.errorsOn = !settingsState.errorsOn
        sayText(event.project, "Error settings", "Errors are ${if (settingsState.errorsOn) "on" else "off"}")
    }
}