package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.openapi.project.Project

interface AudibleQueue {
    fun say(project: Project, value: String, optionalTitle: String?)
    fun say(value: String, optionalTitle: String?)

    fun say(value: String)
}