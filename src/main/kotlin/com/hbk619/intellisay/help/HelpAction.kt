package com.hbk619.intellisay.help

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class HelpAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = HelpDialog()

        if (dialog.showAndGet()) {
            val selectedShortcut = dialog.getSelectedShortcut()
            selectedShortcut.actionPerformed(e)
        }
    }
}