package com.hbk619.intellisay.project

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.UsefulTestCase


class DumbModeAnnouncementTest: BaseTestCase() {

    fun testDumbModeAnnouncesEnterWhenEnabled() {
        AppSettingsState.instance.dumbModeAnnouncementOn = true
        val queue = getAudibleQueue()
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            UsefulTestCase.assertSize(1, queue.calls)
            val call = queue.calls[0]
            UsefulTestCase.assertEquals(listOf(project, "Entering dumb mode", "Dumb mode"), call.args)
        }
    }

    fun testDumbModeDoesNotAnnounceEnterWhenDisabled() {
        AppSettingsState.instance.dumbModeAnnouncementOn = false
        val queue = getAudibleQueue()
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            UsefulTestCase.assertSize(0, queue.calls)
        }
    }
}