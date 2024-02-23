package com.hbk619.intellisay.files

import com.hbk619.intellisay.BaseTestCase
import com.intellij.testFramework.UsefulTestCase

class AutomaticFileNameTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testTriggeringActionTogglesSetting() {
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction("AutomaticFileNameSetting")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf("Reading file names is on", "Automatic file name settings"), call.args)

        myFixture.performEditorAction("AutomaticFileNameSetting")

        UsefulTestCase.assertSize(2, queue.calls)

        val call1 = queue.calls[1]
        UsefulTestCase.assertEquals(listOf("Reading file names is off", "Automatic file name settings"), call1.args)
    }

}