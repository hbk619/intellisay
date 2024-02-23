package com.hbk619.intellisay

import com.hbk619.intellisay.sound.AudibleQueue
import com.hbk619.intellisay.sound.MockAudibleQueue
import com.hbk619.intellisay.sound.MockPlayerQueue
import com.hbk619.intellisay.sound.PlayerQueue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class BaseTestCase: BasePlatformTestCase() {

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

    override fun tearDown() {
        getAudibleQueue().reset()
        getPlayerQueue().reset()
        super.tearDown()
    }
}