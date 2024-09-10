package com.hbk619.intellisay.caret

import com.hbk619.intellisay.python.ParameterListVisitor
import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyParameterList

const val NO_CODE_BLOCK = "Not in a code block"

class CurrentContext : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return sayText(null, "Context", "No project open")
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return sayText(project, "Context", "Editor not focused. Press escape")
        val psiFile: PsiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return sayText(project, "Context", "No file found")

        val language = psiFile.language
        val offset = editor.caretModel.offset
        val infoBuilder = StringBuilder()

        when (language.id) {
            "JAVA" -> {
                val element: PsiElement = psiFile.findElementAt(offset) ?: return sayText(project, "Context", NO_CODE_BLOCK)
                val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
                infoBuilder.append(getMethodName(containingMethod?.name))
                infoBuilder.append(" ")
                if (containingMethod != null ) {
                    if (!containingMethod.parameterList.isEmpty) {
                        infoBuilder.append("Arguments are ")
                    }
                    containingMethod.parameterList.parameters.forEach {
                        infoBuilder.append("${it.name} type is ${it.type.canonicalText} ")
                    }
                    if (containingMethod.returnType != null) {
                        infoBuilder.append("Return type is ${containingMethod.returnType!!.canonicalText} ")
                    }
                }
                infoBuilder.append(" ")
                val containingClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
                if (containingMethod == null && containingClass == null) {
                    infoBuilder.clear().append(NO_CODE_BLOCK)
                } else {
                    infoBuilder.append(getClassName(containingClass?.name))
                }
                sayText(project, "Context", infoBuilder.toString())
            }
            "Python" -> {
                val element: PsiElement = psiFile.findElementAt(offset) ?: return sayText(project, "Context", NO_CODE_BLOCK)
                val containingMethod = PsiTreeUtil.getParentOfType(element, PyFunction::class.java)
                val parameters = PsiTreeUtil.getChildOfType(containingMethod, PyParameterList::class.java)
                infoBuilder.append(getMethodName(containingMethod?.name))
                infoBuilder.append(". ")

                if (parameters != null) {
                    val visitor = ParameterListVisitor(project)
                    val names = visitor.visitPyParameterList(parameters)
                    if (names.isNotEmpty()) {
                        infoBuilder.append("Arguments are ")
                    }
                    names.forEach { (name, type) ->
                        val typeText = if (type.isEmpty()) "no type " else "of type $type, "
                        infoBuilder.append("$name $typeText")
                    }
                }

                infoBuilder.append(". ")
                val containingClass = PsiTreeUtil.getParentOfType(element, PyClass::class.java)
                if (containingMethod == null && containingClass == null) {
                    infoBuilder.clear().append(NO_CODE_BLOCK)
                } else {
                    infoBuilder.append(getClassName(containingClass?.name))
                }
                sayText(project, "Context", infoBuilder.toString())
            }
            else -> {
                if (psiFile.name.endsWith(".py")) {
                    return sayText(project, "Context", "Python file detected but python plugin not enabled")
                }
                return sayText(project, "Context", "Language not supported")
            }
        }
    }

    private fun getClassName(name: String?): String {
        return if (name == null) {
            "No class"
        } else {
            "Class is $name"
        }
    }

    private fun getMethodName(name: String?): String {
        return if (name.isNullOrEmpty()) {
            "Can't identify method"
        } else {
            "Inside method $name"
        }
    }
}