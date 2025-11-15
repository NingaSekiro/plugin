package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import org.aopbuddy.plugin.service.DbSyncService;
import org.jetbrains.builtInWebServer.BuiltInServerOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MermaidPanel {

    private final Project project;


    public MermaidPanel(Project project) {
        this.project = project;

    }

    public JComponent getMermaidPanel() {
        JBCefBrowser jbCefBrowser = new JBCefBrowser();
        jbCefBrowser.loadURL(getPath());
        JComponent component = jbCefBrowser.getComponent();
        component.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                Window window = SwingUtilities.getWindowAncestor(component);
                if (window != null) {
                    window.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            DbSyncService service = project.getService(DbSyncService.class);
                            if (service.isRunning()) {
                                service.stop();
                            }
                        }
                    });
                }
            }
        });
        return component;

    }

    private String getPath() {
        return "http://localhost:" + BuiltInServerOptions.getInstance().getEffectiveBuiltInServerPort() + "/api/aopPlugin/index.html" + "?projectId=" + project.getLocationHash();
    }
}
