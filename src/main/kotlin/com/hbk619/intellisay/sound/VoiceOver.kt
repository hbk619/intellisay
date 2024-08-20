package com.hbk619.intellisay.sound

import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.diagnostic.Logger
import java.nio.charset.Charset

private val log = Logger.getInstance(VoiceOver::class.java)

fun validateVoiceOver() {
    val applescriptCanControlVoiceover = listOf("defaults", "read", "com.apple.VoiceOver4/default", "SCREnableAppleScript")
    val command = GeneralCommandLine(applescriptCanControlVoiceover)
    command.charset = Charset.forName("UTF-8")
    command.setWorkDirectory(".")

    val output = ScriptRunnerUtil.getProcessOutput(command)
    AppSettingsState.instance.useVoiceOver = output.trim() == "1"
    log.debug("Applescript setting $output")
}

class VoiceOver {
}