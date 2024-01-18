package com.hbk619.jetbrainsscreenreader.files

import com.hbk619.jetbrainsscreenreader.sound.Say
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull


class FileEditorListener : FileEditorManagerListener {
    private val queue = BackgroundTaskQueue(null, "Saying file name")

    override fun fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
        val say = Say(source.project, "Saying file name", file.name)

        queue.run(say)
    }
}