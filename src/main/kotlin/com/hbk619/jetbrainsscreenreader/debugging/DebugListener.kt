package com.hbk619.jetbrainsscreenreader.debugging

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.debugger.ui.impl.watch.WatchItemDescriptor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.navigation.EmptyNavigatable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator.XEvaluationCallback
import com.intellij.xdebugger.frame.XFullValueEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodePresentationConfigurator
import java.nio.charset.Charset
import javax.swing.Icon


class DebugListener : AnAction() {
    private val log = Logger.getInstance(DebugListener::class.java)
    private val queue = BackgroundTaskQueue(null, "Evaluating variable")
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return say("No project open", null)

        val session = XDebuggerManager.getInstance(project).currentSession
        val frame = session?.currentStackFrame ?: return say("Not debugging or on a breakpoint", project)

        val editor = event.getData(CommonDataKeys.EDITOR) ?: return say("Editor not focused. Press escape", project)
        val selection = editor.selectionModel
        val selected = selection.selectedText ?: return say("Nothing selected", project)

        frame.evaluator?.evaluate(
            XExpressionImpl(selected, null, null, EvaluationMode.EXPRESSION),
            object : XEvaluationCallback {
                override fun evaluated(childValue: XValue) {
                    println(childValue.toString())
                    if (childValue is JavaValue) {
                        val nameOfType = childValue.getDescriptor().getValue().type().name()
                        log.debug("Got variable $childValue")
                        childValue.computePresentation(object : XValueNodePresentationConfigurator.ConfigurableXValueNodeImpl() {
                            override fun applyPresentation(value: Icon?, presentation: XValuePresentation, p2: Boolean) {
                                if (presentation is JavaValuePresentation) {
                                    presentation.renderValue(object : XValuePresentation.XValueTextRenderer {
                                        override fun renderValue(value: String) {
                                            val f = presentation.javaClass.getDeclaredField("myValueDescriptor")
                                            f.isAccessible = true
                                            val desc = f.get(presentation)
                                            if (desc is WatchItemDescriptor) {
                                                val text: String = if (desc.valueText.isEmpty()) desc.value.toString() else "$nameOfType ${desc.valueText}"
                                                say(text, project)
                                            }
                                        }

                                        override fun renderValue(value: String, ta: TextAttributesKey) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderStringValue(value: String) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderStringValue(value: String, p1: String?, p2: Int) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderNumericValue(value: String) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderKeywordValue(value: String) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderComment(value: String) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderSpecialSymbol(value: String) {
                                            say("$value. type is $nameOfType", project)
                                        }

                                        override fun renderError(value: String) {
                                            say("There was an error. $value", project)
                                        }
                                    })
                                } else {
                                    log.warn("Non java presentation value received: ${presentation.javaClass.name}")
                                }
                            }

                            override fun setFullValueEvaluator(value: XFullValueEvaluator) {
                                say(value.toString(), project)
                            }
                        }, XValuePlace.TREE)
                    } else {
                        log.warn("Non java value received: ${childValue.javaClass.name}")
                    }
                }

                override fun errorOccurred(s: @DialogMessage String) {
                    say("An error occurred. $s", project)
                }
            },
            object : XSourcePosition {
                override fun getLine(): Int {
                    return editor.caretModel.logicalPosition.line
                }

                override fun getOffset(): Int {
                    return 0
                }

                override fun getFile(): VirtualFile {
                    return editor.virtualFile
                }

                override fun createNavigatable(project: Project): Navigatable {
                    return EmptyNavigatable.INSTANCE
                }
            }) ?: return say("No evaluator found, are you on a breakpoint?", project)
    }

    private fun say(value: String, project: Project?) {
        val commands = listOf("say", value)

        val generalCommandLine = GeneralCommandLine(commands)
        generalCommandLine.setCharset(Charset.forName("UTF-8"))
        generalCommandLine.setWorkDirectory(project?.basePath ?: "")

        queue.run(object : Backgroundable(null, "Evaluating code") {
            override fun run(progressIndicator: ProgressIndicator) {
                val output = ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                if (output.isEmpty()) {
                    log.debug("Said $value")
                } else {
                    log.error("Something went wrong saying $value. Output is $output")
                }
            }
        })
    }
}