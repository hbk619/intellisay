package com.hbk619.intellisay.python.interpreter

import com.hbk619.intellisay.dialog.ModalService
import com.hbk619.intellisay.sdk.SDKHelper
import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.sdk.PythonSdkUtil


class SetInterpreterAction: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return sayText(null, "Set Interpreter", "No project open")
        val dialogService = ApplicationManager.getApplication().getService(ModalService::class.java)
        if (dialogService.createAndShowPythonInterpreterDialog()) {
            val sdkHomePath = getSdkHomePath(project, dialogService)
            val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath)
            }

            if (sdkHome == null) {
                return say(project, "Interpreter path $sdkHomePath not found")
            }
            val sdkUtils = ApplicationManager.getApplication().getService(SDKHelper::class.java)
            val sdk = sdkUtils.createPythonSdk(sdkHome)

            if (sdk != null) {
                WriteAction.run<Throwable> {
                    val existingSdk = PythonSdkUtil.findSdkByKey(sdk.name)
                    if (existingSdk == null) {
                        val sdkTable = ProjectJdkTable.getInstance()
                        sdkTable.addJdk(sdk)
                    }
                    val projectRootManager = ProjectRootManager.getInstance(project)
                    projectRootManager.projectSdk = sdk
                }
                say(project, "Python interpreter ${sdk.homePath} will be used as a project SDK")
            }
        }
    }

    private fun getSdkHomePath(
        project: Project,
        interpreterDialog: ModalService
    ): String {
        val userPath = interpreterDialog.getPythonInterpreterLocation()
        return if (userPath.startsWith("/")) {
            userPath
        } else {
            "${project.basePath}/${userPath}/bin/python"
        }
    }

    private fun say(project: Project, text: String) {
        sayText(project, "Set Interpreter", text)
    }
}