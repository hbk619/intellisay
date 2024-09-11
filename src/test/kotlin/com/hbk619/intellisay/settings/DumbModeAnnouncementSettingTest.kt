package com.hbk619.intellisay.settings

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.UsefulTestCase


class DumbModeAnnouncementSettingTest: BaseTestCase() {

    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testDumbModeAnnouncementSettingEnablesWhenDisabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.dumbModeAnnouncementOn = false
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.DumbModeAnnouncementSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Dumb mode announcements are on", "Dumb mode settings"), call.args)
        UsefulTestCase.assertTrue(AppSettingsState.instance.dumbModeAnnouncementOn)
    }

    fun testDumbModeAnnouncementSettingDisablesWhenEnabled() {
        myFixture.configureByFile("src/Main.java")
        AppSettingsState.instance.dumbModeAnnouncementOn = true
        val queue = getAudibleQueue()

        myFixture.performEditorAction("IntelliSay.DumbModeAnnouncementSetting")

        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Dumb mode announcements are off", "Dumb mode settings"), call.args)
        UsefulTestCase.assertFalse(AppSettingsState.instance.dumbModeAnnouncementOn)
    }
}