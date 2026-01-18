package org.aopbuddy.plugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.aopbuddy.plugin.infra.util.PsiKit;
import org.aopbuddy.plugin.service.HeartBeatService;
import org.aopbuddy.plugin.service.WebSocketClientService;
import org.jetbrains.annotations.NotNull;

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

    String qualifiedClassName = method.getContainingClass() == null
        ? null
        : method.getContainingClass().getQualifiedName();
    String methodName = method.getName();
    if (qualifiedClassName == null) {
      return;
    }
    WebSocketClientService ws = project.getService(WebSocketClientService.class);
    ws.sendWatchRequest(qualifiedClassName, methodName, "(..)");
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    Presentation presentation = e.getPresentation();
    if (project == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }
    HeartBeatService heartBeatService = project.getService(HeartBeatService.class);
    presentation.setEnabledAndVisible(heartBeatService.isStatus());
  }
}
