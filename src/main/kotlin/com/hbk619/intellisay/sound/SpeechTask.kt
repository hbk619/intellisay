package com.hbk619.intellisay.sound

import com.hbk619.intellisay.script.Runner
import com.intellij.execution.configurations.GeneralCommandLine
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
    private val runner = ApplicationManager.getApplication().getService(Runner::class.java)

    override fun run(progressIndicator: ProgressIndicator) {
        val output = runner.run(command)
        if (output.isEmpty()) {
            log.debug("Said $command")
        } else {
            log.error("Something went wrong saying $command. Output is $output")
        }
    }
}