package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.sayText
import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.debugger.ui.impl.watch.WatchItemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.xdebugger.frame.presentation.XValuePresentation

class ValueTextRenderer(private val presentation: JavaValuePresentation, private val nameOfType: String?) :
    XValuePresentation.XValueTextRenderer {
    private val log = Logger.getInstance(ValueTextRenderer::class.java)
        override fun renderValue(value: String) {
            val f = presentation.javaClass.getDeclaredField("myValueDescriptor")
            f.isAccessible = true
            val desc = f.get(presentation)
            if (desc is WatchItemDescriptor) {
                val text: String =
                    if (desc.valueText.isEmpty()) desc.value.toString() else "$nameOfType ${desc.valueText}"
                sayText(null, speechTitle, text)
            } else {
                log.warn("Non WatchItemDescriptor received")
                sayText(null, speechTitle, "Not sure what this is I'm afraid")
            }
        }

        override fun renderValue(value: String, ta: TextAttributesKey) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderStringValue(value: String) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderStringValue(value: String, p1: String?, p2: Int) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderNumericValue(value: String) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderKeywordValue(value: String) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderComment(value: String) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderSpecialSymbol(value: String) {
            sayText(null, speechTitle, "$value. type is $nameOfType")
        }

        override fun renderError(value: String) {
            log.error("Error rendering value", value)
            sayText(null, speechTitle, "There was an error. $value")
        }
    }