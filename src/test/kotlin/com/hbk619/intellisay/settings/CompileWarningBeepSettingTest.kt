package com.hbk619.intellisay.settings

import com.hbk619.intellisay.BaseTestCase
import com.intellij.testFramework.UsefulTestCase


class CompileWarningBeepSettingTest: BaseTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testCompileWarningBeepSettingEnablesWhenDisabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.compileWarningsOn = false
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.CompileWarningBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Compile warnings are on", "Compile warning settings"), call.args)
        UsefulTestCase.assertTrue(AppSettingsState.instance.compileWarningsOn)
    }

    fun testCompileWarningBeepSettingDisablesWhenEnabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.compileWarningsOn = true
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.CompileWarningBeepSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Compile warnings are off", "Compile warning settings"), call.args)
        UsefulTestCase.assertFalse(AppSettingsState.instance.compileWarningsOn)
    }
}