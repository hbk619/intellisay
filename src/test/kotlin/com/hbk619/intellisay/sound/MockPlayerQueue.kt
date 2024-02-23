package com.hbk619.intellisay.sound

import com.hbk619.intellisay.mock.Call
import com.hbk619.intellisay.mock.Mock
import com.intellij.openapi.project.Project


class MockPlayerQueue : PlayerQueue, Mock() {
    override fun play(project: Project?, title: String, sound: Sound) {
        addCall(Call(listOf(project, title, sound)))
    }
}