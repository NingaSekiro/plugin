package org.aopbuddy.plugin.toolwindow.view;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField;
import org.jetbrains.plugins.groovy.GroovyFileType;

public class GroovyEditorView {
    private final Project project;

    @Getter
    private final MyEditorTextField groovyEditor;


    public GroovyEditorView(Project project) {
        this.project = project;
        // 编辑器
        this.groovyEditor = new MyEditorTextField(project, GroovyFileType.GROOVY_FILE_TYPE);
        this.groovyEditor.setBorder(JBUI.Borders.empty());
    }
}
