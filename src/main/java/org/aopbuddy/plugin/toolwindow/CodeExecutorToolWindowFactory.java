package org.aopbuddy.plugin.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.aopbuddy.plugin.toolwindow.panel.GroovyConsolePanel;
import org.jetbrains.annotations.NotNull;

public class CodeExecutorToolWindowFactory implements ToolWindowFactory {

    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setStripeTitle("Groovy Console");
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板
        GroovyConsolePanel mainPanel = new GroovyConsolePanel(project);
        // 将面板添加到工具窗口
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        content.setDisposer(mainPanel);
        toolWindow.getContentManager().addContent(content);
    }
}
