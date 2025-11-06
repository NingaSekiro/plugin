package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import javax.swing.*;

@Service(Service.Level.PROJECT)
public final class RecordFrame extends JFrame {
    private final MermaidPanel mermaidPanel;

    public static RecordFrame getInstance(Project project) {
        return project.getService(RecordFrame.class);
    }
    private RecordFrame(Project project) {
        this.mermaidPanel = new MermaidPanel(project);
        this.setTitle("录制");
        this.setSize(1500, 800);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setAlwaysOnTop(false);
        this.add(mermaidPanel.getMermaidPanel());
    }

    public void showWindow() {
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        this.setAlwaysOnTop(false);
    }
}
