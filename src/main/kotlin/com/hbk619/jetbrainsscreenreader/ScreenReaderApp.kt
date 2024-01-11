package com.hbk619.jetbrainsscreenreader

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory;

class ScreenReaderApp : AppLifecycleListener {
    private val LOG: Logger = Logger.getInstance(ScreenReaderApp::class.java)

    override fun appStarted() {
        class DispoableThing : Disposable {
            override fun dispose() {
                LOG.info("Disposed of " + ScreenReaderApp::class.java.name)
            }

        }

        EditorFactory.getInstance().eventMulticaster.addCaretListener(CaretMoveListener(), DispoableThing());
    }
}