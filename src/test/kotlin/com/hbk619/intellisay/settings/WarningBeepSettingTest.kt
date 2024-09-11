package com.hbk619.intellisay.settings

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.UsefulTestCase


class WarningBeepSettingTest: BaseTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testWarningBeepSettingEnablesWhenDisabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.warningsOn = false
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.WarningBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Warnings are on", "Warning settings"), call.args)
        UsefulTestCase.assertTrue(AppSettingsState.instance.warningsOn)
    }

    fun testWarningBeepSettingDisablesWhenEnabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.warningsOn = true
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.WarningBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Warnings are off", "Warning settings"), call.args)
        UsefulTestCase.assertFalse(AppSettingsState.instance.warningsOn)
    }
}