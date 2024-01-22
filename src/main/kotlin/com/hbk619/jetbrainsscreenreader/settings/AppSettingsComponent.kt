package com.hbk619.jetbrainsscreenreader.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val soundsLocation = JBTextField()
    private val warningsOn = JBCheckBox("Beep on warning", true)
    private val errorsOn = JBCheckBox("Beep on error", true)
    private val breakpointsOn = JBCheckBox("Beep on breakpoint", true)
    private val automaticFileNameOn = JBCheckBox("Automatically announce file names", true)

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Sounds location: "), soundsLocation, 1, false)
            .addComponent(warningsOn, 1)
            .addComponent(errorsOn, 1)
            .addComponent(breakpointsOn, 1)
            .addComponent(automaticFileNameOn, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = soundsLocation

    var soundsLocationText: String
        get() = soundsLocation.text
        set(newText) {
            soundsLocation.text = newText
        }

    var isWarningBeepOn: Boolean
        get() = warningsOn.isSelected
        set(newVal) {
            warningsOn.setSelected(newVal)
        }
    var isErrorBeepOn: Boolean
        get() = errorsOn.isSelected
        set(newVal) {
            errorsOn.setSelected(newVal)
        }
    var isBreakpointBeepOn: Boolean
        get() = breakpointsOn.isSelected
        set(newVal) {
            breakpointsOn.setSelected(newVal)
        }

    var isAutomaticFileNameOn: Boolean
        get() = automaticFileNameOn.isSelected
        set(newVal) {
            automaticFileNameOn.setSelected(newVal)
        }
}