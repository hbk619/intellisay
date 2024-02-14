package com.hbk619.jetbrainsscreenreader.sound

import com.hbk619.jetbrainsscreenreader.mock.Call
import com.hbk619.jetbrainsscreenreader.mock.Mock
import com.intellij.openapi.project.Project


class MockPlayerQueue : PlayerQueue, Mock() {
    override fun play(project: Project?, title: String, sound: Sound) {
        addCall(Call(listOf(project, title, sound)))
    }
}