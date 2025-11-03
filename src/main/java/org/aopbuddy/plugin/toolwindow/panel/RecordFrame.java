package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.project.Project;

import javax.swing.*;

public class RecordFrame extends JFrame {
    private static RecordFrame instance;
    private final MermaidPanel mermaidPanel;
    private final Project project;

    public static RecordFrame getInstance(Project project) {
        if (instance == null) {
            instance = new RecordFrame(project);
        }
        instance.setVisible(true);
        instance.setAlwaysOnTop(true);
        instance.setAlwaysOnTop(false);
        return instance;
    }
    private RecordFrame(Project project) {
        this.project = project;
        this.mermaidPanel = new MermaidPanel(project);
        this.setTitle("录制");
        this.setSize(1500, 800);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setAlwaysOnTop(false);
        this.add(mermaidPanel.getMermaidPanel());
        this.setVisible(true);
    }
}
