package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DumbModeAnnouncement : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.dumbModeAnnouncementOn = !settingsState.dumbModeAnnouncementOn
        sayText(event.project, "Dumb mode settings", "Dumb mode announcements are ${if (settingsState.dumbModeAnnouncementOn) "on" else "off"}")
    }
}