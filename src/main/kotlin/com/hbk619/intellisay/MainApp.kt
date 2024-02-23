package com.hbk619.intellisay

import com.hbk619.intellisay.caret.CaretMoveListener
import com.hbk619.intellisay.sound.sayText
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory


class MainApp : AppLifecycleListener {
    private val log: Logger = Logger.getInstance(MainApp::class.java)
    override fun appStarted() {
        class DisposableThing : Disposable {
            override fun dispose() {
                log.info("Disposed of " + MainApp::class.java.name)
            }

        }
        EditorFactory.getInstance().eventMulticaster.addCaretListener(CaretMoveListener(), DisposableThing())

        sayText(null, "Intelli say started", "Intelli Say has started")
    }
}
