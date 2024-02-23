package com.hbk619.intellisay.sound

import com.intellij.openapi.project.Project

interface PlayerQueue {
    fun play(project: Project?, title: String, sound: Sound)
}