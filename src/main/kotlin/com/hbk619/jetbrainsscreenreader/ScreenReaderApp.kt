package com.hbk619.jetbrainsscreenreader

import com.hbk619.jetbrainsscreenreader.caret.CaretMoveListener
import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory


class ScreenReaderApp : AppLifecycleListener {
    private val log: Logger = Logger.getInstance(ScreenReaderApp::class.java)
    override fun appStarted() {
        class DisposableThing : Disposable {
            override fun dispose() {
                log.info("Disposed of " + ScreenReaderApp::class.java.name)
            }

        }
        EditorFactory.getInstance().eventMulticaster.addCaretListener(CaretMoveListener(), DisposableThing())

        sayText(null, "Intelli say started", "Intelli Say has started")
    }
}
