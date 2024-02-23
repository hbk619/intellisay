package com.hbk619.intellisay.files

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.intellij.testFramework.UsefulTestCase

class FileEditorListenerTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    override fun tearDown() {
        super.tearDown()
        AppSettingsState.instance.automaticFileNameOn = false
    }

    fun testSaysNameWhenFileOpensAndEnabled() {
        AppSettingsState.instance.automaticFileNameOn = true
        myFixture.configureByFile("src/Main.java")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(project, "Main.java", "Saying file name"), call.args)
    }
    fun testDoesNotSayNameWhenFileOpensAndDisabled() {
        myFixture.configureByFile("src/Main.java")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(0, queue.calls)
    }
}