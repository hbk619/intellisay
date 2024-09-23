package com.hbk619.intellisay

import com.hbk619.intellisay.dialog.MockModalService
import com.hbk619.intellisay.dialog.ModalService
import com.hbk619.intellisay.script.MockScriptRunner
import com.hbk619.intellisay.script.Runner
import com.hbk619.intellisay.sdk.MockSDKHelper
import com.hbk619.intellisay.sdk.SDKHelper
import com.hbk619.intellisay.settings.AppSettingsState
import com.hbk619.intellisay.sound.AudibleQueue
import com.hbk619.intellisay.sound.MockAudibleQueue
import com.hbk619.intellisay.sound.MockPlayerQueue
import com.hbk619.intellisay.sound.PlayerQueue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class BaseTestCase: BasePlatformTestCase() {

    fun fuzzyMatch(expected: Regex, actual: String) {
        assertTrue("Expected $expected but got $actual", expected.matches(actual))
    }

    fun getAudibleQueue(): MockAudibleQueue {
        val service = ApplicationManager.getApplication().getService(AudibleQueue::class.java)
        if (service is MockAudibleQueue) {
            return service
        } else {
            throw Exception("Real Audible Queue used instead of mock")
        }
    }

    fun getPlayerQueue(): MockPlayerQueue {
        val service = ApplicationManager.getApplication().getService(PlayerQueue::class.java)
        if (service is MockPlayerQueue) {
            return service
        } else {
            throw Exception("Real Player Queue used instead of mock")
        }
    }

    fun getScriptRunner(): MockScriptRunner {
        val service = ApplicationManager.getApplication().getService(Runner::class.java)
        if (service is MockScriptRunner) {
            return service
        } else {
            throw Exception("Real Player Queue used instead of mock")
        }
    }

    override fun setUp() {
        super.setUp()
        AppSettingsState.instance.dumbModeAnnouncementOn = false
    }

    fun getModalService(): MockModalService {
        val service = ApplicationManager.getApplication().getService(ModalService::class.java)
        if (service is MockModalService) {
            return service
        } else {
            throw Exception("Real modal service used instead of mock")
        }
    }

    fun getSDKHelperService(): MockSDKHelper {
        val service = ApplicationManager.getApplication().getService(SDKHelper::class.java)
        if (service is MockSDKHelper) {
            return service
        } else {
            throw Exception("Real modal service used instead of mock")
        }
    }

    override fun tearDown() {
        getAudibleQueue().reset()
        getPlayerQueue().reset()
        getScriptRunner().reset()
        getModalService().reset()
        getSDKHelperService().reset()
        super.tearDown()
    }
}