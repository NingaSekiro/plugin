package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.Disposable;
import org.aopbuddy.plugin.infra.util.I18nUtil;

import javax.swing.*;

@Service(Service.Level.PROJECT)
public final class RecordFrame extends JFrame implements Disposable {
    private final MermaidPanel mermaidPanel;

    public static RecordFrame getInstance(Project project) {
        return project.getService(RecordFrame.class);
    }
    private RecordFrame(Project project) {
        this.mermaidPanel = new MermaidPanel(project);
        this.setTitle(I18nUtil.message("record.frame.title"));
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

    @Override
    public void dispose() {
        super.dispose();
    }
}
