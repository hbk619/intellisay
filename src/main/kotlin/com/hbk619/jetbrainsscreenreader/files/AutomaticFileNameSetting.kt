package com.hbk619.jetbrainsscreenreader.files

import com.hbk619.jetbrainsscreenreader.settings.AppSettingsState
import com.hbk619.jetbrainsscreenreader.sound.AudibleQueue
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager


class AutomaticFileNameSetting : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.automaticFileNameOn = !settingsState.automaticFileNameOn
        val aQueue: AudibleQueue = ApplicationManager.getApplication().getService(AudibleQueue::class.java)
        aQueue.say("Reading file names is ${if (settingsState.automaticFileNameOn) "on" else "off"}", "Automatic file name settings")
    }
}