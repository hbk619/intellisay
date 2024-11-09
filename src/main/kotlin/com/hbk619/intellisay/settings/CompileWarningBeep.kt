package com.hbk619.intellisay.settings

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CompileWarningBeep : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.compileWarningsOn = !settingsState.compileWarningsOn
        sayText(event.project, "Compile warning settings", "Compile warnings are ${if (settingsState.compileWarningsOn) "on" else "off"}")
    }
}