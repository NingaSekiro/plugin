package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.aopbuddy.plugin.infra.keyboardhandler.KeyBoardHandler;
import org.aopbuddy.plugin.service.DbSyncService;
import org.jetbrains.builtInWebServer.BuiltInServerOptions;

public class MermaidPanel {

    private final Project project;


    public MermaidPanel(Project project) {
        this.project = project;

    }

    public JComponent getMermaidPanel() {
        JBCefBrowser jbCefBrowser = new JBCefBrowser();
        jbCefBrowser.loadURL(getPath());
        jbCefBrowser.getJBCefClient().addKeyboardHandler(new KeyBoardHandler(jbCefBrowser), jbCefBrowser.getCefBrowser());
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
