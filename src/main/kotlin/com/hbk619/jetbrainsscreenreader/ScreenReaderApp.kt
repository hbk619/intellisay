package com.hbk619.jetbrainsscreenreader

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataConstants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebuggerManager


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