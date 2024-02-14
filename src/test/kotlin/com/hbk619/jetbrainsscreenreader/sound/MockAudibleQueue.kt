package com.hbk619.jetbrainsscreenreader.sound

import com.hbk619.jetbrainsscreenreader.mock.Call
import com.hbk619.jetbrainsscreenreader.mock.Mock
import com.intellij.openapi.project.Project


class MockAudibleQueue : AudibleQueue, Mock() {
    override fun say(project: Project, value: String, optionalTitle: String?) {
        addCall(Call(listOf(project, value, optionalTitle)))
    }

    override fun say(value: String, optionalTitle: String?) {
        addCall(Call(listOf(value, optionalTitle)))
    }

    override fun say(value: String) {
        addCall(Call(listOf(value)))
    }
}