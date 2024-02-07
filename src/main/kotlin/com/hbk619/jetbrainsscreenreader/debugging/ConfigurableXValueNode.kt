package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.AudibleQueue
import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.openapi.diagnostic.Logger
import com.intellij.xdebugger.frame.XFullValueEvaluator
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodePresentationConfigurator
import javax.swing.Icon

class ConfigurableXValueNode(private val queue: AudibleQueue, private val nameOfType: String?) :
    XValueNodePresentationConfigurator.ConfigurableXValueNodeImpl() {
    private val log = Logger.getInstance(ConfigurableXValueNode::class.java)
    override fun applyPresentation(value: Icon?, presentation: XValuePresentation, p2: Boolean) {
        if (presentation is JavaValuePresentation) {
            presentation.renderValue(ValueTextRenderer(presentation, queue, nameOfType))
        } else {
            log.warn("Non java presentation value received: ${presentation.javaClass.name}")
            queue.say("Not a java value, cannot evaluate. Got ${presentation.javaClass.name}")
        }
    }

    override fun setFullValueEvaluator(value: XFullValueEvaluator) {
        queue.say(value.toString())
    }
}