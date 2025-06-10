package com.hbk619.intellisay.settings

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
    private val compileWarningsOn = JBCheckBox("Beep on compile warning", true)
    private val breakpointsOn = JBCheckBox("Beep on breakpoint", true)
    private val automaticFileNameOn = JBCheckBox("Automatically announce file names", true)
    private val dumbModeAnnouncementOn = JBCheckBox("Automatically announce dumb mode which is when limited features are available", true)
    private val voiceOverOn = JBCheckBox("Use VoiceOver when available", true)
    private val volume = JBTextField("Volume (between 0 and 1)")

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Sounds location: "), soundsLocation, 1, false)
            .addComponent(warningsOn, 1)
            .addComponent(errorsOn, 1)
            .addComponent(compileWarningsOn, 1)
            .addComponent(breakpointsOn, 1)
            .addComponent(automaticFileNameOn, 1)
            .addComponent(dumbModeAnnouncementOn, 1)
            .addComponent(voiceOverOn, 1)
            .addLabeledComponent(JBLabel("Volume"), volume, 1, false)
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
    var isCompileWarningsOn: Boolean
        get() = compileWarningsOn.isSelected
        set(newVal) {
            compileWarningsOn.setSelected(newVal)
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

    var isDumbModeAnnouncementOn: Boolean
        get() = dumbModeAnnouncementOn.isSelected
        set(newVal) {
            dumbModeAnnouncementOn.setSelected(newVal)
        }

    var isUseVoiceOverOn: Boolean
        get() = voiceOverOn.isSelected
        set(newVal) {
            voiceOverOn.setSelected(newVal)
        }

    var volumeNumber: Double
        get() = volume.text.toDouble()
        set(newVal) {
            volume.text = newVal.toString()
        }
}