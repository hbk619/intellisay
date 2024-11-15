package com.hbk619.intellisay.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Intelli Say Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings: AppSettingsState = AppSettingsState.instance
        var modified: Boolean = !mySettingsComponent?.soundsLocationText.equals(settings.soundsLocation)
        modified = modified or (mySettingsComponent!!.isWarningBeepOn != settings.warningsOn)
        modified = modified or (mySettingsComponent!!.isErrorBeepOn != settings.errorsOn)
        modified = modified or (mySettingsComponent!!.isCompileWarningsOn != settings.compileWarningsOn)
        modified = modified or (mySettingsComponent!!.isBreakpointBeepOn != settings.breakpointsOn)
        modified = modified or (mySettingsComponent!!.isAutomaticFileNameOn != settings.automaticFileNameOn)
        modified = modified or (mySettingsComponent!!.isDumbModeAnnouncementOn != settings.dumbModeAnnouncementOn)
        modified = modified or (mySettingsComponent!!.isUseVoiceOverOn != settings.useVoiceOver)
        return modified
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.instance
        settings.soundsLocation = mySettingsComponent?.soundsLocationText ?: ""
        settings.warningsOn = mySettingsComponent?.isWarningBeepOn!!
        settings.errorsOn = mySettingsComponent?.isErrorBeepOn!!
        settings.compileWarningsOn = mySettingsComponent?.isCompileWarningsOn!!
        settings.breakpointsOn = mySettingsComponent?.isBreakpointBeepOn!!
        settings.automaticFileNameOn = mySettingsComponent?.isAutomaticFileNameOn!!
        settings.dumbModeAnnouncementOn = mySettingsComponent?.isDumbModeAnnouncementOn!!
        settings.useVoiceOver = mySettingsComponent?.isUseVoiceOverOn!!
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.instance
        mySettingsComponent?.soundsLocationText = settings.soundsLocation
        mySettingsComponent?.isWarningBeepOn = settings.warningsOn
        mySettingsComponent?.isErrorBeepOn = settings.errorsOn
        mySettingsComponent?.isCompileWarningsOn = settings.compileWarningsOn
        mySettingsComponent?.isBreakpointBeepOn = settings.breakpointsOn
        mySettingsComponent?.isAutomaticFileNameOn = settings.automaticFileNameOn
        mySettingsComponent?.isDumbModeAnnouncementOn = settings.dumbModeAnnouncementOn
        mySettingsComponent?.isUseVoiceOverOn = settings.useVoiceOver
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}