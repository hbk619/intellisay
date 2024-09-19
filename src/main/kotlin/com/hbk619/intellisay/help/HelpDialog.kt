package com.hbk619.intellisay.help

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent


class HelpDialog: DialogWrapper(false) {
    private val shortcutList = JBList<AnAction>()
    init {
        title = "IntelliSay Help"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val am = ActionManager.getInstance()
        val actionIds = am.getActionIdList("IntelliSay")
        val actions = actionIds.map {am.getAction(it) }

        shortcutList.setListData(actions.toTypedArray())
        shortcutList.setSelectionModel(SingleSelectionModel())
        shortcutList.accessibleContext.accessibleName = "Actions"
        shortcutList.cellRenderer = HelpCellRenderer()

        val panel: DialogPanel = panel {
            row {
                label("Select a shortcut to activate")
            }
            row {
                cell(shortcutList).align(AlignX.FILL)
            }
        }
        panel.preferredFocusedComponent = shortcutList

        return panel
    }

    fun getSelectedShortcut(): AnAction {
        return shortcutList.selectedValue
    }
}