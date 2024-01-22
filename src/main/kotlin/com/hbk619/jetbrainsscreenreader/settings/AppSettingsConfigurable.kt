package com.hbk619.jetbrainsscreenreader.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent


internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Intelli Say settings"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): @Nullable JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings: AppSettingsState = AppSettingsState.instance
        var modified: Boolean = !mySettingsComponent?.soundsLocationText.equals(settings.soundsLocation)
        modified = modified or mySettingsComponent!!.isWarningBeepOn != settings.warningsOn
        modified = modified or mySettingsComponent!!.isErrorBeepOn != settings.errorsOn
        modified = modified or mySettingsComponent!!.isBreakpointBeepOn != settings.breakpointsOn
        modified = modified or mySettingsComponent!!.isAutomaticFileNameOn != settings.automaticFileNameOn
        return modified
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.instance
        settings.soundsLocation = mySettingsComponent?.soundsLocationText ?: ""
        settings.warningsOn = mySettingsComponent?.isWarningBeepOn!!
        settings.errorsOn = mySettingsComponent?.isErrorBeepOn!!
        settings.breakpointsOn = mySettingsComponent?.isBreakpointBeepOn!!
        settings.automaticFileNameOn = mySettingsComponent?.isAutomaticFileNameOn!!
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.instance
        mySettingsComponent?.soundsLocationText = settings.soundsLocation
        mySettingsComponent?.isWarningBeepOn = settings.warningsOn
        mySettingsComponent?.isErrorBeepOn = settings.errorsOn
        mySettingsComponent?.isBreakpointBeepOn = settings.breakpointsOn
        mySettingsComponent?.isAutomaticFileNameOn = settings.automaticFileNameOn
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}