package org.aopbuddy.plugin.toolwindow.model;

import com.intellij.openapi.application.ApplicationManager;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.view.GroovyEditorView;

public class RunModel {

  private final JvmService jvmService;
  private final RunResultModel runResultModel;
  private final GroovyEditorView groovyEditorView;

  public RunModel(JvmService jvmService, RunResultModel runResultModel,
      GroovyEditorView groovyEditorView) {
    this.jvmService = jvmService;
    this.runResultModel = runResultModel;
    this.groovyEditorView = groovyEditorView;
  }

  public void eval() {
    // 1. 获取Groovy脚本内容（UI线程安全）
    String scriptContent = groovyEditorView.getGroovyEditor().getText();

    // 2. 在后台线程执行耗时的eval操作
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      String result = jvmService.eval(scriptContent);

      // 3. 将结果更新操作切换回UI线程
      ApplicationManager.getApplication().invokeLater(() -> {
        runResultModel.setStatus(result);
      });
    });
  }
}
