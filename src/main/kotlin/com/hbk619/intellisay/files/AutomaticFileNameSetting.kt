package com.hbk619.intellisay.files

import com.hbk619.intellisay.settings.AppSettingsState
import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class AutomaticFileNameSetting : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.automaticFileNameOn = !settingsState.automaticFileNameOn
        sayText(null, "Automatic file name settings", "Reading file names is ${if (settingsState.automaticFileNameOn) "on" else "off"}", )
    }
}