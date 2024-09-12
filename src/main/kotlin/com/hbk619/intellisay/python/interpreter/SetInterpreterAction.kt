package com.hbk619.intellisay.python.interpreter

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUtil


class SetInterpreterAction: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return sayText(null, "Set Interpreter", "No project open")
        val projectRootManager = ProjectRootManager.getInstance(project)
        val interpreterDialog = InterpreterDialog()
        if (interpreterDialog.showAndGet()) {
            val sdkHomePath = getSdkHomePath(project, interpreterDialog)
            val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath)
            }

            if (sdkHome == null) {
                return say(project, "Interpreter path $sdkHomePath not found")
            }

            val sdk = SdkConfigurationUtil.setupSdk(
                emptyArray(),
                sdkHome,
                PythonSdkType.getInstance(),
                false,
                null,
                sdkHome.path
            )

            if (sdk != null) {
                WriteAction.run<Throwable> {
                    val existingSdk = PythonSdkUtil.findSdkByKey(sdk.name)
                    if (existingSdk == null) {
                        val sdkTable = ProjectJdkTable.getInstance()
                        sdkTable.addJdk(sdk)
                    }
                    projectRootManager.projectSdk = sdk
                }
                say(project, "Python interpreter ${sdk.homePath} will be used as a project SDK")
            }
        }
    }

    private fun getSdkHomePath(
        project: Project,
        interpreterDialog: InterpreterDialog
    ): String {
        val userPath = interpreterDialog.getPythonLocation()
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