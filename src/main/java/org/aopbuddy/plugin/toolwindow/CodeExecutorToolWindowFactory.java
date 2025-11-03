package org.aopbuddy.plugin.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.aopbuddy.plugin.toolwindow.panel.GroovyConsolePanel;
import org.jetbrains.annotations.NotNull;

public class CodeExecutorToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板
        JBTabbedPane tabbedPane = new JBTabbedPane();

        GroovyConsolePanel mainPanel = new GroovyConsolePanel(project);

        // 添加面板到选项卡
        tabbedPane.addTab("Groovy控制台", mainPanel);

        // 将面板添加到工具窗口
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(tabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
