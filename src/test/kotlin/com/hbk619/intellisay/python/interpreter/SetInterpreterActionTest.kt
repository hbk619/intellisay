package com.hbk619.intellisay.python.interpreter

import com.hbk619.intellisay.BaseTestCase
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.UsefulTestCase
import java.awt.event.InputEvent

class SetInterpreterActionTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/pythonplay"
    }

    fun testDoesNotSetProjectInterpreterIfLocalPathInvalidAndAnnounces() {
        myFixture.configureByFile("main.py")
        val modalService = getModalService()
        modalService.pythonPath = "wrong"
        modalService.pythonPathSet = true
        triggerAction()

        val queue = getAudibleQueue()
        assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertContainsOrdered(call.args!!, listOf(project, "Set Interpreter"))
        fuzzyMatch(Regex("Interpreter path /[a-zA-Z0-9/._-]+/wrong/bin/python not found"), call.args[1].toString())
        assertNull(ProjectRootManager.getInstance(project).projectSdk)
    }

    fun testDoesNotSetProjectInterpreterIfAbsolutePathInvalidAndAnnounces() {
        myFixture.configureByFile("main.py")
        val modalService = getModalService()
        modalService.pythonPath = "/only/a/maniac/would/have/this/structure"
        modalService.pythonPathSet = true
        triggerAction()

        val queue = getAudibleQueue()
        assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertContainsOrdered(call.args!!, listOf(project, "Interpreter path /only/a/maniac/would/have/this/structure not found", "Set Interpreter"))
        assertNull(ProjectRootManager.getInstance(project).projectSdk)
    }

    fun testSetsProjectInterpreterIfAbsolutePathValidAndAnnounces() {
        myFixture.configureByFile("main.py")

        val modalService = getModalService()
        val pythonPath = "${System.getProperty("user.dir")}/${myFixture.testDataPath}/venv/bin/python"
        modalService.pythonPath = pythonPath
        modalService.pythonPathSet = true

        triggerAction()

        val queue = getAudibleQueue()
        assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertContainsOrdered(call.args!!, listOf(project, "Python interpreter $pythonPath will be used as a project SDK", "Set Interpreter"))
        assertNotNull(ProjectRootManager.getInstance(project).projectSdk)
    }

    fun testSetsProjectInterpreterIfAbsolutePathValidAndSDKExistsAndAnnounces() {
        myFixture.configureByFile("main.py")
        val projectDir = "${System.getProperty("user.dir")}/${myFixture.testDataPath}"
        VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, projectDir)
        val pythonPath = "${projectDir}/venv/bin/python"
        val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
            LocalFileSystem.getInstance().refreshAndFindFileByPath(pythonPath)
        }
        WriteAction.run<Throwable> {
            sdk = SdkConfigurationUtil.setupSdk(
                emptyArray(),
                sdkHome,
                JavaSdkImpl.getInstance(),
                false,
                null,
                "existing sdk"
            )
            val sdkTable = ProjectJdkTable.getInstance()
            sdkTable.addJdk(sdk!!)
        }

        val modalService = getModalService()
        modalService.pythonPath = pythonPath
        modalService.pythonPathSet = true

        triggerAction()

        val queue = getAudibleQueue()
        assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertContainsOrdered(call.args!!, listOf(project, "Python interpreter $pythonPath will be used as a project SDK", "Set Interpreter"))
        assertNotNull(ProjectRootManager.getInstance(project).projectSdk)
    }

    private var sdk: Sdk? = null

    override fun tearDown() {
        if (sdk != null) {
            WriteAction.run<Throwable> {
                ProjectJdkTable.getInstance().removeJdk(sdk!!)
            }
        }
        super.tearDown()
    }

    private fun triggerAction() {
        val a = SetInterpreterAction()
        val dataContext: DataContext = (myFixture.editor as EditorEx).dataContext
        val managerEx = ActionManagerEx.getInstanceEx()
        val event = AnActionEvent(null as InputEvent?, dataContext, "unknown", Presentation(), managerEx, 0)
        a.actionPerformed(event)
    }
}