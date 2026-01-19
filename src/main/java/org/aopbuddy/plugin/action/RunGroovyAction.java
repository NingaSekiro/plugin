/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.aopbuddy.plugin.action;

import static org.aopbuddy.plugin.toolwindow.view.RunResultView.quickIsJson;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.aopbuddy.plugin.infra.util.IconUtil;
import org.aopbuddy.plugin.service.HeartBeatService;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField;
import org.jetbrains.annotations.NotNull;


public class RunGroovyAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getProject();
    if (project == null) {
      return;
    }
    JvmService jvmService = project.getService(JvmService.class);
    // 获取当前编辑的文件
    Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor == null) {
      return;
    }
    // 保存当前文件
    FileDocumentManager.getInstance().saveDocument(editor.getDocument());
    String content = editor.getDocument().getText();
    ToolWindowManager.getInstance(project).invokeLater(() -> {
      String eval = jvmService.eval(content);
      RunContentManager runContentManager = RunContentManager.getInstance(project);
      Executor executor = DefaultRunExecutor.getRunExecutorInstance();
      // 创建结果展示面板
      String consoleTitle = String.format("Groovy Execution Result-%s",
          editor.getVirtualFile().getName());
      runContentManager.getAllDescriptors().stream()
          .filter(descriptor -> descriptor.getDisplayName().equals(consoleTitle))
          .findFirst()
          .ifPresent(
              contentDescriptor -> runContentManager.removeRunContent(executor, contentDescriptor));
      MyEditorTextField myEditorTextField = new MyEditorTextField(project, JsonFileType.INSTANCE);
      myEditorTextField.setText(eval,
          quickIsJson(eval) ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE);
      // 创建包含结果文本区域的面板
      JPanel resultPanel = new JPanel(new BorderLayout());
      resultPanel.add(myEditorTextField, BorderLayout.CENTER);

      RunContentDescriptor runContentDescriptor = new RunContentDescriptor(null, null,
          resultPanel, consoleTitle) {
        @Override
        public boolean isContentReuseProhibited() {
          return true;
        }

        @Override
        public Icon getIcon() {
          return IconUtil.getPluginIcon();
        }
      };
      runContentManager.showRunContent(executor, runContentDescriptor);
    });
  }

//  用于控制 AnAction.update() 方法的执行线程：
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
    // 当前文件
    PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
    if (file == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }
    presentation.setEnabledAndVisible(
        "Groovy".equalsIgnoreCase(file.getFileType().getName()) && heartBeatService.isStatus());
  }
}
