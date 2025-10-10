package org.aopbuddy.plugin.toolwindow.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import lombok.SneakyThrows;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.service.ConsoleStateService;
import org.aopbuddy.plugin.service.DbSyncService;
import org.aopbuddy.plugin.service.HeartBeatService;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.component.HintComboBox;
import org.aopbuddy.plugin.toolwindow.component.JvmProcessSelectorDialog;
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField;
import org.aopbuddy.plugin.toolwindow.component.RecordConfigDialog;
import org.jetbrains.plugins.groovy.GroovyFileType;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroovyConsolePanel extends OnePixelSplitter {
    private static final Logger LOGGER = Logger.getInstance(GroovyConsolePanel.class);

    private final Project project;
    private final ConsoleStateService consoleStateService;
    private final JvmService jvmService;
    private final DbSyncService dbSyncService;
    private final HeartBeatService heartBeatService;

    private final MyEditorTextField groovyEditor;
    private final JLabel attachLabel;
    private final HintComboBox<String> classloaderComboBox;
    private final MyEditorTextField runStatusEditor;
    private final JToggleButton recordToggleButton;


    public GroovyConsolePanel(Project project) {
        super(false, "JZ.ConsoleRun", 0.6F);
        this.project = project;
        this.consoleStateService = project.getService(ConsoleStateService.class);
        this.jvmService = project.getService(JvmService.class);
        this.dbSyncService = project.getService(DbSyncService.class);
        this.heartBeatService = project.getService(HeartBeatService.class);
        // 编辑器
        this.groovyEditor = new MyEditorTextField(project, GroovyFileType.GROOVY_FILE_TYPE);
        this.groovyEditor.setBorder(JBUI.Borders.empty(5));

        // 标签页
        // 连接状态标签
        this.attachLabel = createAttachLabel();

        // 类加载器下拉框
        this.classloaderComboBox = new HintComboBox<>(200);
        this.classloaderComboBox.setHint("2.选择classLoader");
        this.classloaderComboBox.addActionListener(e -> {
            // 更新状态服务中的选中ClassLoader
            if (classloaderComboBox.getSelectedItem() != null) {
                consoleStateService.setSelectedClassloader(classloaderComboBox.getSelectedItem().toString());
            }
        });


        // 结果面板
        this.runStatusEditor = new MyEditorTextField(project, FileTypes.PLAIN_TEXT);
        this.runStatusEditor.setBorder(JBUI.Borders.empty(5));
        this.recordToggleButton = createRecordToggleButton();
        setFirstComponent(getGroovyConsolePanel());
        setSecondComponent(getJvmResultInfoPanel());
        project.getMessageBus().connect().subscribe(
                ToolWindowUpdateNotifier.GROOVY_CONSOLE_CHANGED_TOPIC,
                (ToolWindowUpdateNotifier) this.groovyEditor::setText);
        // 考虑java bean的监听 替代
        project.getMessageBus().connect().subscribe(
                ToolWindowUpdateNotifier.ATTACH_STATUS_CHANGED_TOPIC, (ToolWindowUpdateNotifier) status -> {
                    this.attachLabel.setText(status.equals("true") ? "已连接" : "未连接");
                    this.attachLabel.setForeground(status.equals("true") ? JBColor.GREEN : JBColor.RED);
                    this.dbSyncService.stop();
                    this.
                            updateClassloaderComboBox();
                }
        );
    }


    private JComponent getGroovyConsolePanel() {
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setPreferredSize(new Dimension(-1, 30));
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, 0));
        toolbarPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
        toolbarPanel.add(this.createAttachButton());
        toolbarPanel.add(this.attachLabel);
        toolbarPanel.add(this.classloaderComboBox);
        toolbarPanel.add(createRunButton());
        toolbarPanel.add(this.recordToggleButton);
        toolbarPanel.add(Box.createHorizontalGlue());
        toolbarPanel.add(Box.createHorizontalStrut(5));
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(JBUI.Borders.empty());
        rootPanel.add(toolbarPanel, "North");
        rootPanel.add(this.groovyEditor, "Center");
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
        toolbarPanel.add(Box.createHorizontalStrut(5));
        // 结果面板
        JBTabbedPane groovyTabbedPane = new JBTabbedPane();
        groovyTabbedPane.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));
        groovyTabbedPane.add("执行方法结果", this.runStatusEditor);
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(JBUI.Borders.empty());
        rootPanel.add(toolbarPanel, "North");
        rootPanel.add(groovyTabbedPane, "Center");
        return rootPanel;
    }

    @SneakyThrows
    private void showJvmProcessSelection() {
        List<String> processList = jvmService.getJvms();
        // 如果没有找到JVM进程
        if (processList.isEmpty()) {
            Messages.showInfoMessage("当前没有找到正在运行的JVM进程", "提示");
            return;
        }
        // 显示选择对话框
        HttpServer httpServer = JvmProcessSelectorDialog.showAndGetSync(processList);
        // 更新状态服务中的IP和端口
        // 如果用户选择了进程
        if (httpServer != null) {
            consoleStateService.setIp(httpServer.getIp());
            consoleStateService.setPort(httpServer.getPort());
            consoleStateService.setServerName(httpServer.getName());
            // 本地附着
            if (httpServer.getName() != null) {
                jvmService.attach(httpServer);
            }
            // 更新连接状态标签
            heartBeatService.setRunning(true);
            heartBeatService.heartBeat();
        }
    }

    private void updateClassloaderComboBox() {
        List<String> classloaders = jvmService.getClassloaders();
        classloaderComboBox.removeAllItems();
        for (String classloader : classloaders) {
            classloaderComboBox.addItem(classloader);
        }
        classloaderComboBox.setSelectedIndex(0);
        // 更新状态服务中的可用ClassLoader列表和选中ClassLoader
        consoleStateService.setAvailableClassloaders(classloaders);
        if (!classloaders.isEmpty()) {
            consoleStateService.setSelectedClassloader(classloaders.get(0));
        }
    }

    private JLabel createAttachLabel() {
        JLabel attachLabel = new JLabel();
        attachLabel.setText("not attached");
        attachLabel.setForeground(JBColor.RED);
        return attachLabel;
    }

    private JButton createAttachButton() {
        JButton amplifierButton = new JButton();
        amplifierButton.setIcon(AllIcons.Actions.AddList);
        amplifierButton.setContentAreaFilled(false);
        amplifierButton.setToolTipText("Attach");
        amplifierButton.setPreferredSize(new Dimension(30, 30));
        amplifierButton.addActionListener(e -> showJvmProcessSelection());
        return amplifierButton;
    }

    private JButton createRunButton() {
        JButton amplifierButton = new JButton();
        amplifierButton.setIcon(AllIcons.Actions.RunAll);
        amplifierButton.setContentAreaFilled(false);
        amplifierButton.setToolTipText("执行");
        amplifierButton.setPreferredSize(new Dimension(30, 30));
        amplifierButton.addActionListener(e -> {
            String result = jvmService.eval(groovyEditor.getText());
            runStatusEditor.setText(result);
        });
        return amplifierButton;
    }

    private JToggleButton createRecordToggleButton() {
        JToggleButton recordButton = new JToggleButton();
        recordButton.setIcon(AllIcons.Actions.StartDebugger); // 使用录制图标
        recordButton.setSelectedIcon(AllIcons.Actions.Suspend); // 录制中使用暂停图标
        recordButton.setContentAreaFilled(false);
        recordButton.setToolTipText("录制数据");
        recordButton.setPreferredSize(new Dimension(30, 30));

        // 记录开始录制时间
        final long[] startTime = {0};
        recordButton.addActionListener(e -> {
            if (recordButton.isSelected()) {
                RecordConfigDialog dialog = new RecordConfigDialog();
                if (!dialog.showAndGet()) {
                    recordButton.setSelected(false);
                    return;
                }
                // 获取对话框输入值（已通过内部校验）
                consoleStateService.setListenerClassName(dialog.getClassName().trim());
                consoleStateService.setListenerMethodName(dialog.getMethodName().trim());
                dbSyncService.record();
                // 记录开始时间并更新UI
                startTime[0] = System.currentTimeMillis();
                recordButton.setToolTipText("停止录制");
                recordButton.setBackground(JBColor.GREEN);

                // 格式化开始时间显示
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                String startTimeStr = dateFormat.format(new Date(startTime[0]));
                runStatusEditor.setText("录制中... 开始时间: " + startTimeStr);
            } else {
                dbSyncService.stop();
                // 计算录制时长并更新UI
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime[0];
                long minutes = duration / 60000;
                long seconds = (duration % 60000) / 1000;

                recordButton.setToolTipText("录制数据");
                recordButton.setBackground(null);

                runStatusEditor.setText(String.format("录制已停止，时长: %d分%d秒", minutes, seconds));
            }
        });
        return recordButton;
    }


    private Component createClearActionButton() {
        JButton clearActionButton = new JButton();
        clearActionButton.setIcon(AllIcons.Actions.GC);
        clearActionButton.setContentAreaFilled(false);
        clearActionButton.setToolTipText("清除执行结果");
        clearActionButton.setPreferredSize(new Dimension(30, -1));
        return clearActionButton;
    }
}
