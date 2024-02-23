package com.hbk619.intellisay.files

import com.hbk619.intellisay.BaseTestCase
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.testFramework.UsefulTestCase

class FileNameListenerTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testSayFileNameWhenFileOpened() {
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction("FileNameListener")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Main.java", "Saying file name"), call.args)
    }

    fun testWarnsWhenFileNotOpen() {
        myFixture.configureByFile("src/Main.java")

        FileEditorManagerEx.getInstanceEx(project).closeAllFiles()
        myFixture.performEditorAction("FileNameListener")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Editor not focused. Press escape", "Saying file name"), call.args)
    }
}