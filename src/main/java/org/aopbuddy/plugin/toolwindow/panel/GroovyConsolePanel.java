package org.aopbuddy.plugin.toolwindow.panel;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.*;

import lombok.SneakyThrows;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.infra.model.EvalRequest;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.component.HintComboBox;
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField;
import org.jetbrains.plugins.groovy.GroovyFileType;

public class GroovyConsolePanel extends OnePixelSplitter {
    public static final String RUN_TIP_FORMAT = "Status: %s, Agent: %sms, Method: %sms";

    private final Project project;

    private JvmService jvmService = Singleton.get(JvmService.class);


    private final MyEditorTextField groovyEditor;

    private final HintComboBox<String> pidComboBox;


    private final HintComboBox<String> classloaderComboBox;

    private final MyEditorTextField runStatusEditor;


    public GroovyConsolePanel(Project project) {
        super(false, "JZ.ConsoleRun", 0.6F);
        this.project = project;
        // 编辑器
        this.groovyEditor = new MyEditorTextField(project, GroovyFileType.GROOVY_FILE_TYPE);
        this.groovyEditor.setBorder(JBUI.Borders.empty(5));

        // 标签页
        // pid连接情况
        this.pidComboBox = new HintComboBox<>(200);
        this.pidComboBox.addItem("1.点此添加JVM进程");
        this.pidComboBox.setSelectedIndex(0);
        this.pidComboBox.addActionListener(e -> {
            if (pidComboBox.getSelectedIndex() == 0) {
                showJvmProcessSelection();
            } else {
                updateClassloaderComboBox();
            }
        });
        // 类加载器下拉框
        this.classloaderComboBox = new HintComboBox<>(200);
        this.classloaderComboBox.setHint("2.选择classLoader");

        // 结果面板
        this.runStatusEditor = new MyEditorTextField(project, FileTypes.PLAIN_TEXT);
        this.runStatusEditor.setBorder(JBUI.Borders.empty(5));
        setFirstComponent(getGroovyConsolePanel());
        setSecondComponent(getJvmResultInfoPanel());
        project.getMessageBus().connect().subscribe(
                ToolWindowUpdateNotifier.UPDATE_TOPIC,
                (ToolWindowUpdateNotifier) this.groovyEditor::setText);

    }


    private JComponent getGroovyConsolePanel() {
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setPreferredSize(new Dimension(-1, 30));
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, 0));
        toolbarPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
        toolbarPanel.add(this.pidComboBox);
        toolbarPanel.add(this.classloaderComboBox);
        toolbarPanel.add(createRunButton());
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
        JPanel runStatusPanel = new JPanel(new BorderLayout());
        runStatusPanel.setBorder(JBUI.Borders.empty());
        runStatusPanel.add(this.runStatusEditor, "Center");
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(JBUI.Borders.empty());
        rootPanel.add(toolbarPanel, "North");
        rootPanel.add(runStatusPanel, "Center");
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
        String selectedProcess = Messages.showEditableChooseDialog(
                "请选择要连接的JVM进程:",
                "选择JVM进程",
                Messages.getQuestionIcon(),
                processList.toArray(new String[0]),
                processList.get(0), // 默认选择第一个
                null
        );

        // 如果用户选择了进程
        if (selectedProcess != null) {
            // 从选择的文本中提取PID（第一个空格前的部分）
            // 将选中的PID添加到ComboBox中
            jvmService.attach(selectedProcess);
            updatePidComboBox(selectedProcess);
        }

    }

    private void updatePidComboBox(String selectedProcess) {
        // 检查PID是否已存在于ComboBox中
        boolean exists = false;
        for (int i = 0; i < pidComboBox.getItemCount(); i++) {
            String item = pidComboBox.getItemAt(i);
            if (item != null && item.equals(selectedProcess)) {
                exists = true;
                break;
            }
        }

        // 如果不存在，则添加到ComboBox中
        if (!exists) {
            pidComboBox.addItem(selectedProcess);
        }

        // 选择该PID
        pidComboBox.setSelectedItem(selectedProcess);
    }

    private void updateClassloaderComboBox() {
        JvmService jvmService = Singleton.get(JvmService.class);
        List<String> classloaders = jvmService.getClassloaders(pidComboBox.getSelectedItem().toString());
        classloaderComboBox.removeAllItems();
        for (String classloader : classloaders) {
            classloaderComboBox.addItem(classloader);
        }
        classloaderComboBox.setSelectedIndex(0);
    }


    private JButton createRunButton() {
        JButton amplifierButton = new JButton();
        amplifierButton.setIcon(AllIcons.Actions.RunAll);
        amplifierButton.setContentAreaFilled(false);
        amplifierButton.setToolTipText("执行");
        amplifierButton.setPreferredSize(new Dimension(30, 30));
        amplifierButton.addActionListener(e -> {
            String script = groovyEditor.getText();
            String classloader = classloaderComboBox.getSelectedItem().toString();
            String pid = pidComboBox.getSelectedItem().toString().split(" - ")[0];
            if (StrUtil.isEmpty(script) || StrUtil.isEmpty(classloader) || StrUtil.isEmpty(script)) {
                Messages.showInfoMessage("pid,classloader,script不能为空", "提示");
                return;
            }
            EvalRequest evalRequest = new EvalRequest(pid, classloader, script);
            String result = jvmService.eval(pidComboBox.getSelectedItem().toString(), evalRequest);
            runStatusEditor.setText(result);
        });
        return amplifierButton;
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
