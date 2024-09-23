package com.hbk619.intellisay.dialog

interface ModalService {
    fun createAndShowPythonInterpreterDialog(): Boolean

    fun getPythonInterpreterLocation(): String
}