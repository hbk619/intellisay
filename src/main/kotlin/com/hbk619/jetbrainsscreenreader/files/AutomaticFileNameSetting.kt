package com.hbk619.jetbrainsscreenreader.files

import com.hbk619.jetbrainsscreenreader.settings.AppSettingsState
import com.hbk619.jetbrainsscreenreader.sound.AudibleQueue
import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager


class AutomaticFileNameSetting : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.automaticFileNameOn = !settingsState.automaticFileNameOn
        sayText(null, "Automatic file name settings", "Reading file names is ${if (settingsState.automaticFileNameOn) "on" else "off"}", )
    }
}