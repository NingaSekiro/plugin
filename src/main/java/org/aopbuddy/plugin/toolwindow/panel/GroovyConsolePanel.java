package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.service.ConsoleStateService;
import org.aopbuddy.plugin.service.DbSyncService;
import org.aopbuddy.plugin.service.HeartBeatService;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.model.*;
import org.aopbuddy.plugin.toolwindow.view.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GroovyConsolePanel extends OnePixelSplitter implements Disposable {

  private static final Logger LOGGER = Logger.getInstance(GroovyConsolePanel.class);

  private final Project project;
  private final ConsoleStateService consoleStateService;
  private final JvmService jvmService;
  private final DbSyncService dbSyncService;
  private final HeartBeatService heartBeatService;

  private final GroovyEditorView groovyEditorView;
  private final ClassloaderView classloaderView;
  private final ClassloaderModel classloaderModel;
  private final RunResultView runResultView;
  private final RunResultModel runResultModel = new RunResultModel();
  private final RecordModel recordModel;
  private final AttachView attachView;
  private final AttachModel attachModel;
  private final RunView runView;
  private final RunModel runModel;


  public GroovyConsolePanel(Project project) {
    super(false, "JZ.ConsoleRun", 0.6F);
    this.project = project;
    this.consoleStateService = project.getService(ConsoleStateService.class);
    this.jvmService = project.getService(JvmService.class);
    this.dbSyncService = project.getService(DbSyncService.class);
    this.heartBeatService = project.getService(HeartBeatService.class);

    this.groovyEditorView = new GroovyEditorView(project);
    this.runResultView = new RunResultView(project, runResultModel);
    this.recordModel = new RecordModel(consoleStateService, dbSyncService);
    this.attachModel = new AttachModel(jvmService, heartBeatService, consoleStateService);
    this.attachView = new AttachView(attachModel);
    this.runModel = new RunModel(jvmService, runResultModel, groovyEditorView);
    this.runView = new RunView(runModel);
    this.classloaderModel = new ClassloaderModel(consoleStateService);
    this.classloaderView = new ClassloaderView(classloaderModel);
    setBorder(BorderFactory.createEmptyBorder());
    setFirstComponent(getGroovyConsolePanel());
    setSecondComponent(getJvmResultInfoPanel());
    project.getMessageBus().connect(this).subscribe(
        ToolWindowUpdateNotifier.GROOVY_CONSOLE_CHANGED_TOPIC,
        (ToolWindowUpdateNotifier) this.groovyEditorView.getGroovyEditor()::setText);
    // 考虑java bean的监听 替代
    project.getMessageBus().connect(this).subscribe(
        ToolWindowUpdateNotifier.ATTACH_STATUS_CHANGED_TOPIC,
        (ToolWindowUpdateNotifier) message -> {
          boolean status = Boolean.parseBoolean(message);
          if (!status) {
            this.dbSyncService.reInit();
            this.heartBeatService.setRunning(false);
            this.classloaderModel.setClassloaders(new ArrayList<>());
            this.classloaderModel.setSelectedItem(null);
            consoleStateService.setIp(null);
          } else {
            this.updateClassloaderComboBox();
          }
          this.attachModel.setStatus(status);
        }
    );
  }

  @Override
  public void dispose() {
    if (attachView != null) {
      attachView.dispose();
    }
  }


  private JComponent getGroovyConsolePanel() {
    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setPreferredSize(new Dimension(-1, 30));
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, 0));
    toolbarPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
    toolbarPanel.add(this.attachView.getAttachButton());
    toolbarPanel.add(this.classloaderView.getClassloaderComboBox());
    toolbarPanel.add(this.runView.getRunButton());
    toolbarPanel.add(createRecordActionButton());
    toolbarPanel.add(Box.createHorizontalGlue());
    toolbarPanel.add(Box.createHorizontalStrut(5));
    JPanel rootPanel = new JPanel(new BorderLayout());
    rootPanel.setBorder(JBUI.Borders.empty());
    rootPanel.add(toolbarPanel, "North");
    rootPanel.add(this.groovyEditorView.getGroovyEditor(), "Center");
    return rootPanel;
  }

  private JComponent getJvmResultInfoPanel() {
    JPanel toolbarPanel = new JPanel();
    // 右侧顶部工具栏
    toolbarPanel.setPreferredSize(new Dimension(-1, 30));
    toolbarPanel.setLayout(new BoxLayout(toolbarPanel, 0));
    toolbarPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
    toolbarPanel.add(createClearActionButton());
    toolbarPanel.add(Box.createHorizontalGlue());
//        toolbarPanel.add(Box.createHorizontalStrut(5));
    // 结果面板
    JBTabbedPane groovyTabbedPane = new JBTabbedPane();
    groovyTabbedPane.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));
    groovyTabbedPane.add("执行方法结果", this.runResultView.getRunStatusEditor());
    JPanel rootPanel = new JPanel(new BorderLayout());
    rootPanel.setBorder(JBUI.Borders.empty());
    rootPanel.add(toolbarPanel, "North");
    rootPanel.add(groovyTabbedPane, "Center");
    return rootPanel;
  }


  private void updateClassloaderComboBox() {
    List<String> classloaders = jvmService.getClassloaders();
    classloaders.removeIf(classloader -> classloader.contains("bytebuddy")||classloader.contains("AopAgent"));
    classloaderModel.setClassloaders(classloaders);
    if (!classloaders.isEmpty()) {
      classloaderModel.setSelectedItem(classloaders.get(0));
      consoleStateService.setSelectedClassloader(classloaders.get(0));
    }
  }


  private Component createRecordActionButton() {
    JButton recordActionButton = new JButton();
    recordActionButton.setIcon(AllIcons.CodeWithMe.CwmCamOn);
    recordActionButton.setContentAreaFilled(false);
    recordActionButton.setToolTipText("录制");
    recordActionButton.setPreferredSize(new Dimension(30, -1));
    recordActionButton.addActionListener(e -> {
      RecordFrame instance = RecordFrame.getInstance(this.project);
      instance.showWindow();
    });
    return recordActionButton;
  }

  private Component createClearActionButton() {
    JButton clearActionButton = new JButton();
    clearActionButton.setIcon(AllIcons.Actions.GC);
    clearActionButton.setContentAreaFilled(false);
    clearActionButton.setToolTipText("清除执行结果");
    clearActionButton.setPreferredSize(new Dimension(30, -1));
    clearActionButton.addActionListener(e -> {
      this.runResultModel.setStatus("");
    });
    return clearActionButton;
  }
}
