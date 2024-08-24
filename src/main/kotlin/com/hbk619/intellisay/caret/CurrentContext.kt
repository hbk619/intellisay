package com.hbk619.intellisay.caret

import com.hbk619.intellisay.sound.sayText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyFunction


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
                val element: PsiElement = psiFile.findElementAt(offset) ?: return sayText(project, "Context", "No element found")
                val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
                infoBuilder.append(getMethodName(containingMethod?.name))
                infoBuilder.append(" ")
                val containingClass = containingMethod?.containingClass
                infoBuilder.append(getClassName(containingClass?.name))
                sayText(project, "Context", infoBuilder.toString())
            }
            "Python" -> {
                val element: PsiElement = psiFile.findElementAt(offset) ?: return sayText(project, "Context", "No element found")
                val containingMethod = PsiTreeUtil.getParentOfType(element, PyFunction::class.java)
                infoBuilder.append(getMethodName(containingMethod?.name))
                infoBuilder.append(" ")
                val containingClass = containingMethod?.containingClass
                infoBuilder.append(getClassName(containingClass?.name))
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
        return if (name!!.isEmpty()) {
            "Can't identify method, go left"
        } else {
            "Inside method $name"
        }
    }
}