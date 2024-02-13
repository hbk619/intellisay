package com.hbk619.jetbrainsscreenreader.settings

import com.hbk619.jetbrainsscreenreader.sound.say
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue

class BreakpointBeep : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Playing sound")
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.breakpointsOn = !settingsState.breakpointsOn
        val speech = say(event.project, "Breakpoint settings", "Breakpoints are ${if (settingsState.breakpointsOn) "on" else "off"}")
        queue.run(speech)
    }
}