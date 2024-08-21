package com.hbk619.intellisay.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.hbk619.jetbrainsscreenreader.settings.AppSettingsState", storages = [Storage("intellisay.xml")])
internal class AppSettingsState : PersistentStateComponent<AppSettingsState?> {
    var soundsLocation: String = ""
    var warningsOn: Boolean = true
    var errorsOn: Boolean = true
    var breakpointsOn: Boolean = true
    var automaticFileNameOn: Boolean = false
    var useVoiceOver: Boolean = false
    var dumbModeAnnouncementOn: Boolean = true

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}