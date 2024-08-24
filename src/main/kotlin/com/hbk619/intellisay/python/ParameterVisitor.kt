package com.hbk619.intellisay.python

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.impl.ParamHelper.ParamVisitor
import com.jetbrains.python.psi.types.TypeEvalContext

class ParameterVisitor(val project: Project): ParamVisitor() {
    private var names = mutableMapOf<String, String>()
    override fun visitNamedParameter(param: PyNamedParameter?, first: Boolean, last: Boolean) {
        if (param != null && !param.name.isNullOrEmpty() ) {
            val type = param.getArgumentType(TypeEvalContext.codeInsightFallback(project))
            if (param.isKeywordContainer) {
                names.put(param.name!!, "keyword arguments")
            } else if (param.isKeywordOnly) {
                names.put(param.name!!, "${type?.name ?: ""} keyword only")
            } else {
                names.put(param.name!!, type?.name ?: "")
            }
        }
    }

    fun getNames(): Map<String, String> {
        return names
    }
}