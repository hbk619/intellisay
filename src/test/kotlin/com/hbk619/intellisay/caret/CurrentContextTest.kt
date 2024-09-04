package com.hbk619.intellisay.caret

import com.hbk619.intellisay.BaseTestCase
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.UsefulTestCase


class CurrentContextTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testInsideJavaMethod() {
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction("IntelliSay.AnnounceContext")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        UsefulTestCase.assertEquals(listOf(myFixture.project, "Inside method main Arguments are args type is String[] Return type is void  Class is Main", "Context"), queue.calls[0].args)
    }

    fun testInsideJavaMethodEndOfLine() {
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_LINE_END)
        myFixture.performEditorAction("IntelliSay.AnnounceContext")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        UsefulTestCase.assertEquals(listOf(myFixture.project, "Inside method main Arguments are args type is String[] Return type is void  Class is Main", "Context"), queue.calls[0].args)
    }

    fun testInsideJavaClassButNotMethod() {
        myFixture.configureByFile("src/Main.java")

        repeat(3) {
            myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        }
        myFixture.performEditorAction("IntelliSay.AnnounceContext")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        UsefulTestCase.assertEquals(listOf(myFixture.project, "Can't identify method  Class is Main", "Context"), queue.calls[0].args)
    }

    fun testOutsideJavaClassOnWhitespace() {
        myFixture.configureByFile("src/Main.java")

        repeat(6) {
            myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)
        }

        myFixture.performEditorAction("IntelliSay.AnnounceContext")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        UsefulTestCase.assertEquals(listOf(myFixture.project, "Not in a code block", "Context"), queue.calls[0].args)
    }

    fun testOutsideJavaClassEndOfFile() {
        myFixture.configureByFile("src/Main.java")

        repeat(6) {
            myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        }

        myFixture.performEditorAction("IntelliSay.AnnounceContext")

        val queue = getAudibleQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        UsefulTestCase.assertEquals(listOf(myFixture.project, "Not in a code block", "Context"), queue.calls[0].args)
    }
}