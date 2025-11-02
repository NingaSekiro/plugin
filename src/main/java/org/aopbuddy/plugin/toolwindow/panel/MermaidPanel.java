package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.builtInWebServer.BuiltInServerOptions;

import javax.swing.*;

public class MermaidPanel {

    private final Project project;


    public MermaidPanel(Project project) {
        this.project = project;

    }

    public JComponent getMermaidPanel() {
        // 创建浏览器实例
        JBCefBrowser jbCefBrowser = new JBCefBrowser();
        jbCefBrowser.loadURL(getPath());
        return jbCefBrowser.getComponent();

    }

    private String getPath() {
        return "http://localhost:" + BuiltInServerOptions.getInstance().getEffectiveBuiltInServerPort() + "/api/aopPlugin/index.html" + "?projectId=" + project.getLocationHash();
    }
}
