package com.hbk619.jetbrainsscreenreader.settings

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue

class BreakpointBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.breakpointsOn = !settingsState.breakpointsOn
        sayText(event.project, "Breakpoint settings", "Breakpoints are ${if (settingsState.breakpointsOn) "on" else "off"}")
    }
}