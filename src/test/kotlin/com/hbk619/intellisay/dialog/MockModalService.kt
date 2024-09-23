package com.hbk619.intellisay.dialog

import com.hbk619.intellisay.mock.Call
import com.hbk619.intellisay.mock.Mock

class MockModalService: ModalService, Mock() {
    var pythonPath = ""
    var pythonPathSet = false

    override fun createAndShowPythonInterpreterDialog(): Boolean {
        addCall(Call(null))
        return pythonPathSet
    }

    override fun getPythonInterpreterLocation(): String {
        addCall(Call(null))
        return pythonPath
    }
}