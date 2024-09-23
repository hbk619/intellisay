package com.hbk619.intellisay.dialog

import com.hbk619.intellisay.python.interpreter.InterpreterDialog

class DialogService: ModalService {
    private var interpreterDialog: InterpreterDialog? = null

    override fun createAndShowPythonInterpreterDialog(): Boolean {
        interpreterDialog = InterpreterDialog()
        return interpreterDialog!!.showAndGet()
    }

    override fun getPythonInterpreterLocation(): String {
        return interpreterDialog?.getPythonLocation() ?: ""
    }
}