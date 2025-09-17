package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import org.jetbrains.annotations.NotNull;


public class JvmResultPanel extends JBScrollPane {
  private final Project project;

  
  public JvmResultPanel(@NotNull Project project) {
    setBorder(JBUI.Borders.empty());
    this.project = project;
    setVerticalScrollBarPolicy(20);
    setHorizontalScrollBarPolicy(30);
  }
}
