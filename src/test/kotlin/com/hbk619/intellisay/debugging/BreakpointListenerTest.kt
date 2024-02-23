package com.hbk619.intellisay.debugging

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.sound.Sound
import com.intellij.testFramework.UsefulTestCase

class BreakpointListenerTest: BaseTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    fun testToggleBreakpointPlaysSound() {
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction("ToggleLineBreakpoint")

        val queue = getPlayerQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(null, "Playing sound", Sound.BREAKPOINT_ADDED), call.args)

        myFixture.performEditorAction("ToggleLineBreakpoint")

        UsefulTestCase.assertSize(2, queue.calls)

        val call1 = queue.calls[1]
        UsefulTestCase.assertEquals(listOf(null, "Playing sound", Sound.BREAKPOINT_REMOVED), call1.args)
    }
}