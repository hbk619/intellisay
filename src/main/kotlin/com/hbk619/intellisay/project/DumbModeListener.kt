package com.hbk619.intellisay.project

import com.hbk619.intellisay.settings.AppSettingsState
import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm
import org.jetbrains.annotations.NotNull


const val TIMEOUT: Int = 500

class DumbModeListener(@field:NotNull private var project: Project) : DumbService.DumbModeListener {
    private var myAlarm = Alarm()

    override fun enteredDumbMode() {
        announce("Entering dumb mode")

        DumbService.getInstance(project).runReadActionInSmartMode {
            runAfterDumb(project)
        }
    }

    private fun runAfterDumb(project: Project) {
        DumbService.getInstance(project).smartInvokeLater {
            myAlarm.addRequest({
                if (DumbService.isDumb(project)) {
                    runAfterDumb(project)
                } else {
                    announce("Exiting dumb mode")
                }
            }, TIMEOUT)
        }
    }

    private fun announce(text: String) {
        if (AppSettingsState.instance.dumbModeAnnouncementOn) {
            sayText(project, "Dumb mode", text)
        }
    }
}