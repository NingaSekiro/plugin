package org.aopbuddy.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.infra.util.PsiKit;

public class WatchMethodAction extends AnAction {

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
        String codeSnippet = generatePrivateMethodCall(method);
        ToolWindowUpdateNotifier publisher = project.getMessageBus()
                .syncPublisher(ToolWindowUpdateNotifier.GROOVY_CONSOLE_CHANGED_TOPIC);
        publisher.onUpdate(codeSnippet);
    }


    private String generatePrivateMethodCall(PsiMethod method) {
        StringBuilder sb = new StringBuilder();

        PsiClass containingClass = method.getContainingClass();
        String className = containingClass.getQualifiedName();

        // 添加必要的导入
        sb.append("addListener(\"").append(className).append("\", \"").append(method.getName()).append("\");\n");
        return sb.toString();
    }
}
