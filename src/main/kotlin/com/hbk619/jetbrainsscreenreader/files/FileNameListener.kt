package com.hbk619.jetbrainsscreenreader.files

import com.hbk619.jetbrainsscreenreader.sound.say
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project


class FileNameListener : AnAction() {
    private val queue = BackgroundTaskQueue(null, "Saying file name")
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return say("No project open", null)

        val editor = event.getData(CommonDataKeys.EDITOR) ?: return say("Editor not focused. Press escape", project)

        say(editor.virtualFile.name, project)
    }

    private fun say(value: String, project: Project?) {
        val speech = say(project, "Saying file name", value)

        queue.run(speech)
    }
}