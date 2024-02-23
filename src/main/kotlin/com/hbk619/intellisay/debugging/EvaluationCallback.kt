package com.hbk619.intellisay.debugging

import com.hbk619.intellisay.sound.sayText
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.NlsContexts
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValuePlace

class EvaluationCallback() :
    XDebuggerEvaluator.XEvaluationCallback {
    private val log = Logger.getInstance(EvaluationCallback::class.java)
    override fun evaluated(childValue: XValue) {
        if (childValue is JavaValue) {
            val nameOfType = childValue.descriptor.value.type().name()
            log.debug("Got variable $childValue")
            childValue.computePresentation(ConfigurableXValueNode(nameOfType), XValuePlace.TREE)
        } else {
            try {
                val f = childValue.javaClass.getDeclaredField("myValue")
                f.isAccessible = true
                val desc = f.get(childValue)
                val t = childValue.javaClass.getDeclaredField("myType")
                t.isAccessible = true
                val type = t.get(childValue)
                sayText(null, speechTitle, "$desc type is $type")
                log.warn("Non java value received was actually ${childValue.javaClass.name}")
            } catch (e: Exception) {
                errorOccurred("Failed to get type $e")
            }
        }
    }

    override fun errorOccurred(s: @NlsContexts.DialogMessage String) {
        log.error("An error occurred while computing presentation", s)
        sayText(null, speechTitle, "An error occurred. $s")
    }
}