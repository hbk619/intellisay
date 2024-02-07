package com.hbk619.jetbrainsscreenreader.sound

interface AudibleQueue {
    fun say(value: String, optionalTitle: String?)

    fun say(value: String)
}