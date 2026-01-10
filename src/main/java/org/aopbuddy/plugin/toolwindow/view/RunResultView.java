package org.aopbuddy.plugin.toolwindow.view;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.aopbuddy.plugin.toolwindow.StatusChangeListener;
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField;
import org.aopbuddy.plugin.toolwindow.model.RunResultModel;

public class RunResultView implements StatusChangeListener<String> {

  private final RunResultModel runResultModel;

  @Getter
  private final MyEditorTextField runStatusEditor;

  public RunResultView(Project project, RunResultModel model) {
    this.runResultModel = model;
    this.runStatusEditor = new MyEditorTextField(project, JsonFileType.INSTANCE);
    this.runStatusEditor.setBorder(JBUI.Borders.empty());
//    this.runStatusEditor.setViewer(true);
    runResultModel.addStatusChangeListener(this);
  }

  @Override
  public void onStatusChanged(String newStatus) {
    runStatusEditor.setText(newStatus,
        quickIsJson(newStatus) ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE);
  }


  /**
   * 快速判断字符串是否可能是合法的 JSON 格式（仅基于开头结尾字符和简单排除） 注意：这只是粗略判断，不是严格的 JSON 验证
   */
  public static boolean quickIsJson(String str) {
    if (str == null) {
      return false;
    }
    str = str.trim();
    // 长度小于 2 肯定不是 JSON
    if (str.length() < 2) {
      return false;
    }

    char first = str.charAt(0);
    char last = str.charAt(str.length() - 1);
    // JSON 必须以 { 或 [ 开头，以 } 或 ] 结尾

    return (first == '{' && last == '}') ||
        (first == '[' && last == ']');
  }
}
