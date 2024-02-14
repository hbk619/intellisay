package com.hbk619.jetbrainsscreenreader.settings

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue

class WarningBeep : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Playing sound")
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.warningsOn = !settingsState.warningsOn
        val speech = sayText(event.project, "Warning settings", "Warnings are ${if (settingsState.warningsOn) "on" else "off"}")
        queue.run(speech)
    }
}