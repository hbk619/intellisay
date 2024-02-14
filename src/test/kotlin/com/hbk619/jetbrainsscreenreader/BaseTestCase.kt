package com.hbk619.jetbrainsscreenreader

import com.hbk619.jetbrainsscreenreader.sound.AudibleQueue
import com.hbk619.jetbrainsscreenreader.sound.MockAudibleQueue
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
}