package com.hbk619.intellisay.help

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.ui.CellRendererPanel
import com.intellij.util.system.OS
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class HelpCellRenderer : ListCellRenderer<AnAction> {
    private val macKeys = mapOf("meta" to "command", "alt" to "option")
    private val itemLabel = JLabel("")

    private val panel = CellRendererPanel().apply {
        add(itemLabel)
    }
    override fun getListCellRendererComponent(
        list: JList<out AnAction>?,
        value: AnAction?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val shortcut = value?.shortcutSet?.shortcuts?.map { it }?.joinToString("") ?: ""
        var formattedShortcut = shortcut.replace("pressed ", "")
            .replace(Regex("^\\[(.+)]$"), "$1")
            .replace("ctrl", "control")

        if (OS.CURRENT == OS.macOS) {
            formattedShortcut = replaceText(formattedShortcut, macKeys)
        }
        itemLabel.text = "${value?.templateText} Shortcut is $formattedShortcut"
        panel.accessibleContext.accessibleName = value?.templateText
        panel.accessibleContext.accessibleDescription = formattedShortcut
        return panel
    }

    private fun replaceText(text: String, map: Map<String, String>): String {
       return map.entries.fold(text) { acc, (key, value) -> acc.replace(key, value) }
    }
}