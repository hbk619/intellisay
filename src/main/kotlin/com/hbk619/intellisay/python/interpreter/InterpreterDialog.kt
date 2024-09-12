package com.hbk619.intellisay.python.interpreter

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField

class InterpreterDialog: DialogWrapper(false) {

    private val pathInput = JTextField()

    init {
        title = "Interpreter Location"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel: DialogPanel = panel {
            row {
                label("Enter python location")
            }
            row {
                cell(pathInput).align(AlignX.FILL)
            }
        }

        pathInput.text = "venv"
        panel.preferredFocusedComponent = pathInput
        return panel
    }

    fun getPythonLocation(): String {
        return pathInput.text
    }
}