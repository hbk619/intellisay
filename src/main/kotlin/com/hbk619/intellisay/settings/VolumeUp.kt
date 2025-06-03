package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class VolumeUp : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.volume += 0.2
        sayText(event.project, "Volume settings", "Sound volume increased")
    }
}