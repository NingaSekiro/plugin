package org.aopbuddy.plugin.toolwindow.view;

import com.intellij.openapi.fileTypes.FileTypes;
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
        this.runStatusEditor = new MyEditorTextField(project, FileTypes.PLAIN_TEXT);
        this.runStatusEditor.setBorder(JBUI.Borders.empty(5));
        runResultModel.addStatusChangeListener(this);
    }

    @Override
    public void onStatusChanged(String newStatus) {
        runStatusEditor.setText(newStatus);
    }
}
