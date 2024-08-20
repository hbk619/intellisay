package com.hbk619.intellisay.sound

import com.hbk619.intellisay.script.Runner
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
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
    AppSettingsState.instance.useVoiceOver = output.trim() == "1"
    log.debug("Applescript setting $output")
}

class VoiceOver {
}