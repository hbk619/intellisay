package com.hbk619.intellisay.files

import com.hbk619.intellisay.settings.AppSettingsState
import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull


class FileEditorListener : FileEditorManagerListener {
    override fun fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
        if (AppSettingsState.instance.automaticFileNameOn) {
            sayText(source.project, "Saying file name", file.name)
        }
    }
}