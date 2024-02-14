package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.openapi.diagnostic.Logger
import com.intellij.xdebugger.frame.XFullValueEvaluator
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodePresentationConfigurator
import javax.swing.Icon

class ConfigurableXValueNode(private val nameOfType: String?) :
    XValueNodePresentationConfigurator.ConfigurableXValueNodeImpl() {
    private val log = Logger.getInstance(ConfigurableXValueNode::class.java)
    override fun applyPresentation(value: Icon?, presentation: XValuePresentation, p2: Boolean) {
        if (presentation is JavaValuePresentation) {
            presentation.renderValue(ValueTextRenderer(presentation, nameOfType))
        } else {
            log.warn("Non java presentation value received: ${presentation.javaClass.name}")
            sayText(null, speechTitle, "Not a java value, cannot evaluate. Got ${presentation.javaClass.name}")
        }
    }

    override fun setFullValueEvaluator(value: XFullValueEvaluator) {
        sayText(null, speechTitle, value.toString())
    }
}