package com.hbk619.intellisay.settings

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.UsefulTestCase


class ErrorBeepSettingTest: BaseTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testErrorBeepSettingEnablesWhenDisabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.errorsOn = false
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.ErrorBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Errors are on", "Error settings"), call.args)
        UsefulTestCase.assertTrue(AppSettingsState.instance.errorsOn)
    }

    fun testErrorBeepSettingDisablesWhenEnabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.errorsOn = true
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.ErrorBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Errors are off", "Error settings"), call.args)
        UsefulTestCase.assertFalse(AppSettingsState.instance.errorsOn)
    }
}