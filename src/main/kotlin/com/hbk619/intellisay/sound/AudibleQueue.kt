package com.hbk619.intellisay.sound

import com.intellij.openapi.project.Project

interface AudibleQueue {
    fun say(project: Project, value: String, optionalTitle: String?)
    fun say(value: String, optionalTitle: String?)

    fun say(value: String)
}