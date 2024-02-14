package com.hbk619.jetbrainsscreenreader.sound

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

fun sayText(project: Project?, title: String, text: String) {
    val aQueue: AudibleQueue = ApplicationManager.getApplication().getService(AudibleQueue::class.java)
    if (project != null) aQueue.say(project, text, title) else aQueue.say(text, title)
}

class SpeechTask(project: Project?, title: String, private val command: GeneralCommandLine) : Task.Backgroundable(project, title) {
    private val log = Logger.getInstance(SpeechTask::class.java)

    override fun run(progressIndicator: ProgressIndicator) {
        val output = ScriptRunnerUtil.getProcessOutput(command)
        if (output.isEmpty()) {
            log.debug("Said $command")
        } else {
            log.error("Something went wrong saying $command. Output is $output")
        }
    }
}