package com.hbk619.intellisay.script

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil

class ScriptRunner: Runner {
    override fun run(command: GeneralCommandLine): String {
        return ScriptRunnerUtil.getProcessOutput(command)
    }
}