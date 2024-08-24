package com.hbk619.intellisay.python

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyParameterList
import com.jetbrains.python.psi.impl.ParamHelper

class ParameterListVisitor(val project: Project) {
    fun visitPyParameterList(node: PyParameterList): Map<String, String> {
        val visitor = ParameterVisitor(project)
        ParamHelper.walkDownParamArray(node.parameters, visitor)
        return visitor.getNames()
    }
}