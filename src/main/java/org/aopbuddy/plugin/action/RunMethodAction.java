package org.aopbuddy.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.infra.util.PsiKit;

public class RunMethodAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (project == null || editor == null || psiFile == null) {
      return;
    }
    PsiMethod method = PsiKit.getTargetMethod(editor, psiFile);
    if (method == null) {
      return;
    }
    // 检查方法的可见性
    String codeSnippet;
//        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
    // 对于公共方法，生成简单的调用表达式
    codeSnippet = generatePublicMethodCall(method);
//        } else {
    // 对于私有或受保护方法，生成反射调用表达式
//            codeSnippet = generatePrivateMethodCall(method);
//        }
    ToolWindowUpdateNotifier publisher = project.getMessageBus()
        .syncPublisher(ToolWindowUpdateNotifier.GROOVY_CONSOLE_CHANGED_TOPIC);
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
      boolean isStatic = method.hasModifierProperty(PsiModifier.STATIC);
      if (isStatic) {
        // 静态方法：直接使用类名调用
        sb.append("toJson(").append(simpleClassName).append(".").append(method.getName())
            .append("());");
      } else {
        // 非静态方法：使用实例调用
        String variableName =
            Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
        sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(")
            .append(simpleClassName).append(".class);\n");
        sb.append("toJson(").append(variableName).append(".").append(method.getName())
            .append("());");
      }

    }

    return sb.toString();
  }

  /**
   * 生成私有方法调用代码片段（使用反射）
   */
  private String generatePrivateMethodCall(PsiMethod method) {
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
      String variableName =
          Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);

      sb.append(simpleClassName).append(" ").append(variableName).append(" = getObject(")
          .append(simpleClassName).append(".class);\n");
      sb.append("Method method = ").append(simpleClassName).append(".class.getDeclaredMethod(\"")
          .append(method.getName()).append("\");\n");
      sb.append("method.setAccessible(true);\n");
      sb.append("//method.invoke(").append(variableName).append(");\n");
      sb.append("toJson(method.invoke(").append(variableName).append("));");
    }

    return sb.toString();
  }
}
