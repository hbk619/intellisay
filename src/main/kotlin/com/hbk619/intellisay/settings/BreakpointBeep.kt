package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class BreakpointBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.breakpointsOn = !settingsState.breakpointsOn
        sayText(event.project, "Breakpoint settings", "Breakpoints are ${if (settingsState.breakpointsOn) "on" else "off"}")
    }
}