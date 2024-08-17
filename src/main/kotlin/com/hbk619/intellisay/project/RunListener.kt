package com.hbk619.intellisay.project

import com.hbk619.intellisay.sound.sayText
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment


class RunListener : ExecutionListener {
    override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
        sayText(env.project, "Exit code", "Process finished with code $exitCode")
    }
}