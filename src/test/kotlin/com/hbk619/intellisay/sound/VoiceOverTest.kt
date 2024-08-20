package com.hbk619.intellisay.sound

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.testFramework.UsefulTestCase

class VoiceOverTest : BaseTestCase() {
    fun testValidateVoiceOverEnablesWhenAppleScriptEnabled() {
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "1"

        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args[0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertTrue(AppSettingsState.instance.useVoiceOver)
    }

    fun testValidateVoiceOverDisablesWhenAppleScriptDisabled() {
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "0"

        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args[0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)
    }
}