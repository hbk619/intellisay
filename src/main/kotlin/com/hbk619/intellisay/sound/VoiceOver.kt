package com.hbk619.intellisay.sound

import com.hbk619.intellisay.script.Runner
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import java.nio.charset.Charset

private val log = Logger.getInstance(VoiceOver::class.java)

fun validateVoiceOver() {
    val applescriptCanControlVoiceover = listOf("defaults", "read", "com.apple.VoiceOver4/default", "SCREnableAppleScript")
    val command = GeneralCommandLine(applescriptCanControlVoiceover)
    command.charset = Charset.forName("UTF-8")
    command.setWorkDirectory(".")

    val runner = ApplicationManager.getApplication().getService(Runner::class.java)
    val output = runner.run(command)
    if (AppSettingsState.instance.useVoiceOver and (output.trim() != "1")) {
        AppSettingsState.instance.useVoiceOver = false
        log.warn("Use VoiceOver set to tru but Allow VoiceOver to be controlled by AppleScript output is not one. Was ${output.trim()}")
        sayText(null, "Voiceover not configured", "Use VoiceOver is set to true but Allow VoiceOver to be controlled by AppleScript is not set. Please use VoiceOver Utility to allow  VoiceOver to be controlled by AppleScript")
    }
    log.debug("Applescript setting $output")
}

class VoiceOver: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsState = AppSettingsState.instance
        settingsState.useVoiceOver = !settingsState.useVoiceOver
        sayText(event.project, "VoiceOver setting", "VoiceOver is ${if (settingsState.useVoiceOver) "on" else "off"}")
    }
}