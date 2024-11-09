package com.hbk619.intellisay.project

import com.hbk619.intellisay.sound.Sound
import com.hbk619.intellisay.sound.playSound
import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.diagnostic.Logger

class CompilationListener : CompilationStatusListener {
    private val log: Logger = Logger.getInstance(CompilationListener::class.java)

    override fun compilationFinished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
        val projectState = ProjectState.instance
        if (errors > 0) {
            log.debug("Compilation finished with $errors errors")
            playSound(compileContext.project, "Compile error", Sound.COMPILE_ERROR)
            projectState.lastCompileErrors =
                compileContext.getMessages(CompilerMessageCategory.ERROR).toList()
        } else {
            projectState.lastCompileErrors = listOf()
        }
        if (warnings > 0) {
            log.debug("Compilation finished with $warnings warning")
            playSound(compileContext.project, "Compile warning", Sound.COMPILE_WARNING)
            projectState.lastCompileWarnings =
                compileContext.getMessages(CompilerMessageCategory.WARNING).toList()
        } else {
            projectState.lastCompileWarnings = listOf()
        }
    }
}