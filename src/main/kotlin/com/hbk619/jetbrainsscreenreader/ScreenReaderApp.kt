package com.hbk619.jetbrainsscreenreader

import com.hbk619.jetbrainsscreenreader.debugging.BreakpointsListener
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.ProjectManager
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
        val proj = ProjectManager.getInstance().openProjects.find { p -> p.isOpen } ?: return
        XDebuggerManager.getInstance(proj).breakpointManager.addBreakpointListener(BreakpointsListener());
    }
}
