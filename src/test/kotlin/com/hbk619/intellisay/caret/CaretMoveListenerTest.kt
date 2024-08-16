package com.hbk619.intellisay.caret

import com.hbk619.intellisay.BaseTestCase
import com.hbk619.intellisay.settings.AppSettingsState
import com.hbk619.intellisay.sound.Sound
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.EditorFactory
import com.intellij.testFramework.UsefulTestCase

class CaretMoveListenerTest : BaseTestCase() {
    private lateinit var caretMoveListener: CaretMoveListener
    override fun getTestDataPath(): String {
        return "src/test/testdata/java-play"
    }

    override fun setUp() {
        super.setUp()
        caretMoveListener = attachListener()
    }

    override fun tearDown() {
        AppSettingsState.instance.breakpointsOn = true
        AppSettingsState.instance.errorsOn = true
        AppSettingsState.instance.warningsOn = true
        EditorFactory.getInstance().eventMulticaster.removeCaretListener(caretMoveListener)
        super.tearDown()
    }

    fun testMovingThroughLinesWithErrorPlaysSoundsWhenEnabled() {
        AppSettingsState.instance.errorsOn = true
        AppSettingsState.instance.warningsOn = true
        myFixture.configureByFile("src/Main.java")
        myFixture.doHighlighting()

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)

        val queue = getPlayerQueue()
        UsefulTestCase.assertSize(1, queue.calls)

        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(null, "Caret move sound", Sound.ERROR), call.args)

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT)

        UsefulTestCase.assertSize(3, queue.calls)

        val call1 = queue.calls[1]
        UsefulTestCase.assertEquals(listOf(null, "Caret move sound", Sound.ERROR), call1.args)

        val call2 = queue.calls[2]
        UsefulTestCase.assertEquals(listOf(null, "Caret move sound", Sound.WARNING), call2.args)

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        UsefulTestCase.assertSize(4, queue.calls)
    }

    fun testMovingThroughLinesWithErrorDoesNotPlaySoundsWhenErrorsDisabled() {
        AppSettingsState.instance.errorsOn = false
        AppSettingsState.instance.warningsOn = true
        myFixture.configureByFile("src/Main.java")
        myFixture.doHighlighting()

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)

        val queue = getPlayerQueue()
        UsefulTestCase.assertSize(0, queue.calls)

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT)

        UsefulTestCase.assertSize(0, queue.calls)

        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        UsefulTestCase.assertSize(0, queue.calls)
    }

    fun testToggleBreakpointDoesNotPlaySoundIfSettingIsDisabled() {
        AppSettingsState.instance.breakpointsOn = false
        myFixture.configureByFile("src/App.java")

        myFixture.performEditorAction("ToggleLineBreakpoint")
        val queue = getPlayerQueue()
        queue.reset()
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)

        UsefulTestCase.assertSize(0, queue.calls)
    }

    fun testToggleBreakpointPlaysSoundIfSettingIsEnabled() {
        AppSettingsState.instance.breakpointsOn = true
        myFixture.configureByFile("src/Main.java")

        myFixture.performEditorAction("ToggleLineBreakpoint")
        val queue = getPlayerQueue()
        queue.reset()
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        UsefulTestCase.assertSize(0, queue.calls)
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)
        UsefulTestCase.assertSize(1, queue.calls)
        val call = queue.calls[0]
        UsefulTestCase.assertEquals(listOf(null, "Caret move sound", Sound.BREAKPOINT), call.args)
    }

    private fun attachListener(): CaretMoveListener {
        class DisposableDo : Disposable {
            override fun dispose() {
            }

        }

        val caretMoveListener = CaretMoveListener()
        EditorFactory.getInstance().eventMulticaster.addCaretListener(caretMoveListener, DisposableDo())
        return caretMoveListener
    }
}