package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class WarningBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.warningsOn = !settingsState.warningsOn
        sayText(event.project, "Warning settings", "Warnings are ${if (settingsState.warningsOn) "on" else "off"}")
    }
}