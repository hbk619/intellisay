package com.hbk619.intellisay.sound

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.testFramework.UsefulTestCase

class VoiceOverTest : BaseTestCase() {
    fun testValidateVoiceOverLeavesEnabledWhenAppleScriptEnabled() {
        AppSettingsState.instance.useVoiceOver = true
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "1"

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args!![0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertTrue(AppSettingsState.instance.useVoiceOver)
    }
    fun testValidateVoiceOverAnnouncesAndDisablesWhenAppleScriptDisabledAndVoiceOverEnabled() {
        AppSettingsState.instance.useVoiceOver = true
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "0"

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args!![0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)
        UsefulTestCase.assertEquals(listOf("Use VoiceOver is set to true but Allow VoiceOver to be controlled by AppleScript is not set. Please use VoiceOver Utility to allow  VoiceOver to be controlled by AppleScript", "Voiceover not configured"), queue.calls[0].args)
    }

    fun testValidateVoiceOverDisablesWhenAppleScriptDisabled() {
        AppSettingsState.instance.useVoiceOver = true
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "0"

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args!![0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)
    }

    fun testValidateVoiceOverLeavesDisableDWhenAppleScriptDisabled() {
        AppSettingsState.instance.useVoiceOver = false
        val mockRunner = getScriptRunner()
        mockRunner.returnValue = "0"

        validateVoiceOver()

        val calls = mockRunner.calls
        assertEquals(1, calls.size)
        UsefulTestCase.assertSize(1, calls)
        val commandLine = calls[0].args!![0] as GeneralCommandLine

        UsefulTestCase.assertEquals("defaults read com.apple.VoiceOver4/default SCREnableAppleScript" , commandLine.commandLineString)
        UsefulTestCase.assertEquals(Charsets.UTF_8, commandLine.charset)
        UsefulTestCase.assertFalse(AppSettingsState.instance.useVoiceOver)
    }
}