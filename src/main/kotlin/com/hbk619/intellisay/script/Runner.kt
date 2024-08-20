package com.hbk619.intellisay.script

import com.intellij.execution.configurations.GeneralCommandLine

interface Runner {
    fun run(command: GeneralCommandLine): String
}