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

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Sounds location: "), soundsLocation, 1, false)
            .addComponent(warningsOn, 1)
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
}