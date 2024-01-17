package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.nio.charset.Charset

fun Say(project: Project?, title: String, text: String): Speech {
    val commands = listOf("say", text)
    val command = GeneralCommandLine(commands)
    command.setCharset(Charset.forName("UTF-8"))
    command.setWorkDirectory(project?.basePath ?: "")

    return Speech(project, title, command)
}
class Speech(project: Project?, title: String, private val command: GeneralCommandLine) : Task.Backgroundable(project, title) {
    private val log = Logger.getInstance(Speech::class.java)

    override fun run(progressIndicator: ProgressIndicator) {
        val output = ScriptRunnerUtil.getProcessOutput(command)
        if (output.isEmpty()) {
            log.debug("Said $command")
        } else {
            log.error("Something went wrong saying $command. Output is $output")
        }
    }
}