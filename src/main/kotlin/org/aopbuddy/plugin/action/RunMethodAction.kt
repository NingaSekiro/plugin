package org.aopbuddy.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.PsiTreeUtil
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier

class RunMethodAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        if (project == null || editor == null || psiFile == null) {
            return
        }
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)
        if (element == null) {
            return
        }
        // 查找包含的 PsiMethod
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)

        if (method == null) {
            Messages.showMessageDialog(project, "请将光标放置在方法内部", "提示", Messages.getInformationIcon())
            return
        }

        // 检查方法的可见性
        val codeSnippet: String
        codeSnippet = if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            // 对于公共方法，生成简单的调用表达式
            generatePublicMethodCall(method)
        } else if (method.hasModifierProperty(PsiModifier.PRIVATE) || method.hasModifierProperty(PsiModifier.PROTECTED)) {
            // 对于私有或受保护方法，生成反射调用表达式
            generatePrivateMethodCall(method, psiFile)
        } else {
            "// 无法识别的方法类型"
        }

        val publisher = project.messageBus
            .syncPublisher(ToolWindowUpdateNotifier.UPDATE_TOPIC)
        publisher.onUpdate(codeSnippet)
    }

    /**
     * 生成公共方法调用代码片段
     */
    private fun generatePublicMethodCall(method: PsiMethod): String {
        val sb = StringBuilder()

        // 添加导入语句（如果需要）
        val containingClass = method.containingClass
        if (containingClass != null) {
            val className = containingClass.qualifiedName
            if (className != null) {
                sb.append("import ").append(className).append(";\n\n")
            }

            // 创建对象和调用方法
            val simpleClassName = containingClass.name ?: return "// 无法获取类名"
            val variableName = simpleClassName[0].lowercaseChar() + simpleClassName.substring(1)

            sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(").append(simpleClassName).append(".class);\n")
            sb.append("//").append(variableName).append(".").append(method.name).append("();\n")
            sb.append("toJson(").append(variableName).append(".").append(method.name).append("());")
        }

        return sb.toString()
    }

    /**
     * 生成私有方法调用代码片段（使用反射）
     */
    private fun generatePrivateMethodCall(method: PsiMethod, psiFile: com.intellij.psi.PsiFile): String {
        val sb = StringBuilder()

        val containingClass = method.containingClass
        if (containingClass != null) {
            val className = containingClass.qualifiedName
            val simpleClassName = containingClass.name ?: return "// 无法获取类名"

            // 添加必要的导入
            sb.append("import java.lang.reflect.Method;\n")
            if (className != null) {
                sb.append("import ").append(className).append(";\n\n")
            }

            // 生成反射调用代码
            val variableName = simpleClassName[0].lowercaseChar() + simpleClassName.substring(1)

            sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(").append(simpleClassName).append(".class);\n")
            sb.append("Method method = ").append(simpleClassName).append(".class.getDeclaredMethod(\"").append(method.name).append("\");\n")
            sb.append("method.setAccessible(true);\n")
            sb.append("//method.invoke(").append(variableName).append(");\n")
            sb.append("toJson(method.invoke(").append(variableName).append("));")
        }

        return sb.toString()
    }
}