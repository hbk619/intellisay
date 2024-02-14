package com.hbk619.jetbrainsscreenreader.files

import com.hbk619.jetbrainsscreenreader.settings.AppSettingsState
import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.BackgroundTaskQueue


class AutomaticFileNameSetting : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Playing sound")
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.automaticFileNameOn = !settingsState.automaticFileNameOn
        val speech = sayText(event.project, "Automatic file name settings", "Reading file names is ${if (settingsState.automaticFileNameOn) "on" else "off"}")
        queue.run(speech)
    }
}