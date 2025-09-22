package org.aopbuddy.plugin1.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.ui.Messages;
import org.aopbuddy.plugin1.infra.ToolWindowUpdateNotifier;

public class RunMethodAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || psiFile == null) {
            return;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return;
        }
        // 查找包含的 PsiMethod
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        
        if (method == null) {
            Messages.showMessageDialog(project, "请将光标放置在方法内部", "提示", Messages.getInformationIcon());
            return;
        }
        
        // 检查方法的可见性
        String codeSnippet;
        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            // 对于公共方法，生成简单的调用表达式
            codeSnippet = generatePublicMethodCall(method);
        } else if (method.hasModifierProperty(PsiModifier.PRIVATE) || method.hasModifierProperty(PsiModifier.PROTECTED)) {
            // 对于私有或受保护方法，生成反射调用表达式
            codeSnippet = generatePrivateMethodCall(method, psiFile);
        } else {
            codeSnippet = "// 无法识别的方法类型";
        }

        ToolWindowUpdateNotifier publisher = project.getMessageBus()
                .syncPublisher(ToolWindowUpdateNotifier.UPDATE_TOPIC);
        publisher.onUpdate(codeSnippet);
    }
    
    /**
     * 生成公共方法调用代码片段
     */
    private String generatePublicMethodCall(PsiMethod method) {
        StringBuilder sb = new StringBuilder();
        
        // 添加导入语句（如果需要）
        PsiClass containingClass = method.getContainingClass();
        if (containingClass != null) {
            String className = containingClass.getQualifiedName();
            if (className != null) {
                sb.append("import ").append(className).append(";\n\n");
            }
            
            // 创建对象和调用方法
            String simpleClassName = containingClass.getName();
            String variableName = Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
            
            sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(").append(simpleClassName).append(".class);\n");
            sb.append("//").append(variableName).append(".").append(method.getName()).append("();\n");
            sb.append("toJson(").append(variableName).append(".").append(method.getName()).append("());");
        }
        
        return sb.toString();
    }
    
    /**
     * 生成私有方法调用代码片段（使用反射）
     */
    private String generatePrivateMethodCall(PsiMethod method, PsiFile psiFile) {
        StringBuilder sb = new StringBuilder();
        
        PsiClass containingClass = method.getContainingClass();
        if (containingClass != null) {
            String className = containingClass.getQualifiedName();
            String simpleClassName = containingClass.getName();
            
            // 添加必要的导入
            sb.append("import java.lang.reflect.Method;\n");
            if (className != null) {
                sb.append("import ").append(className).append(";\n\n");
            }
            
            // 生成反射调用代码
            String variableName = Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
            
            sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(").append(simpleClassName).append(".class);\n");
            sb.append("Method method = ").append(simpleClassName).append(".class.getDeclaredMethod(\"").append(method.getName()).append("\");\n");
            sb.append("method.setAccessible(true);\n");
            sb.append("//method.invoke(").append(variableName).append(");\n");
            sb.append("toJson(method.invoke(").append(variableName).append("));");
        }
        
        return sb.toString();
    }
}
