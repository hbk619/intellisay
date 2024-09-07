package com.hbk619.intellisay.settings

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.UsefulTestCase


class BreakpointBeepSettingTest: BaseTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testBreakpointBeepSettingEnablesWhenDisabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.breakpointsOn = false
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.BreakpointBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Breakpoints are on", "Breakpoint settings"), call.args)
        UsefulTestCase.assertTrue(AppSettingsState.instance.breakpointsOn)
    }

    fun testBreakpointBeepSettingDisablesWhenEnabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.breakpointsOn = true
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.BreakpointBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Breakpoints are off", "Breakpoint settings"), call.args)
        UsefulTestCase.assertFalse(AppSettingsState.instance.breakpointsOn)
    }
}