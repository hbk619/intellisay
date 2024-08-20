package com.hbk619.intellisay.script

import com.hbk619.intellisay.mock.Call
import com.hbk619.intellisay.mock.Mock
import com.intellij.execution.configurations.GeneralCommandLine

class MockScriptRunner: Runner, Mock() {

    var returnValue = ""

    override fun run(command: GeneralCommandLine): String {
        addCall(Call(listOf(command)))
        return returnValue
    }
}