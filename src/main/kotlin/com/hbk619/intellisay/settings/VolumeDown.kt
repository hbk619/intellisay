package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class VolumeDown : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        if (settingsState.volume <= 0) {
            sayText(event.project, "Volume settings", "Minimum sound volume reached")
        } else {
            settingsState.volume -= 0.1
            sayText(event.project, "Volume settings", "Sound volume decreased")
        }
    }
}