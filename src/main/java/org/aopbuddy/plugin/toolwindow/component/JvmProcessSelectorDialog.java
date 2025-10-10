package org.aopbuddy.plugin.toolwindow.component;

import cn.hutool.core.lang.Validator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 进程选择对话框组件
 * 使用 JBTabbedPane 分成两个面板：
 * - 本地进程：从提供的进程列表中选择
 * - 远程 JVM：输入 IP 地址（格式：IP:端口）
 * 确定后返回选择的进程字符串（本地）或 IP 字符串（远程），否则 null
 * 只能选择一个面板的值
 */
public class JvmProcessSelectorDialog extends DialogWrapper {

    private final JBTabbedPane tabbedPane = new JBTabbedPane();
    private final JPanel localPanel = new JPanel(new BorderLayout());
    private final JPanel remotePanel = new JPanel(new BorderLayout());
    private final JComboBox<String> localComboBox; // 使用标准 JComboBox
    private final JBTextField remoteTextField = new JBTextField();
    // 获取返回值（在 doOKAction 后调用）
    @Getter
    private HttpServer selectedValue = null;

    public JvmProcessSelectorDialog(List<String> processList) {
        super(true);
        // 初始化本地进程下拉框
        String[] processArray = processList.toArray(new String[0]);
        this.localComboBox = new JComboBox<>(processArray);

        setTitle("选择 JVM 连接方式");
        initValidation();
        init();
        setupUI();
    }

    private void setupUI() {
        // 本地进程面板
        JBLabel localLabel = new JBLabel("请选择本地 JVM 进程:");
        localLabel.setBorder(JBUI.Borders.empty(10));
        localPanel.add(localLabel, BorderLayout.NORTH);

        // 使用 JComboBox 显示进程列表
        localComboBox.setPreferredSize(new Dimension(300, 30));
        localPanel.add(localComboBox, BorderLayout.CENTER);

        // 远程 JVM 面板
        JBLabel remoteLabel = new JBLabel(
                "请输入远程JVM IP和agent http port端口 (e.g., 192.168.1.1:8888),java agent需预先安装，默认端口8888:");
        remoteLabel.setBorder(JBUI.Borders.empty(10));
        remotePanel.add(remoteLabel, BorderLayout.NORTH);

        remoteTextField.setPreferredSize(new Dimension(300, 30));
        remotePanel.add(remoteTextField, BorderLayout.CENTER);

        // 添加到 TabbedPane
        tabbedPane.addTab("本地进程", localPanel);
        tabbedPane.addTab("远程 JVM", remotePanel);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return tabbedPane;
    }

    @Override
    protected void doOKAction() {
        // 根据当前 tab 验证并设置返回值
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0 -> { // 本地进程
                selectedValue = new HttpServer((String) localComboBox.getSelectedItem(), "127.0.0.1", 8888);
            }
            case 1 -> { // 远程 JVM
                selectedValue = new HttpServer(null, remoteTextField.getText().split(":")[0], Integer.parseInt(remoteTextField.getText().split(":")[1]));
            }
        }
        super.doOKAction();
    }

    // 自定义验证（可选，增强 UI 反馈）
    @Override
    protected @Nullable ValidationInfo doValidate() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0 -> {
                if (localComboBox.getSelectedItem() == null) {
                    return new ValidationInfo("请选择本地进程", localComboBox);
                }
                return null;
            }
            case 1 -> {
                String serverName = remoteTextField.getText();
                if (serverName == null || serverName.trim().isEmpty()) {
                    return new ValidationInfo("请输入 IP:端口", remoteTextField);
                } else {
                    String[] parts = serverName.split(":");
                    if (parts.length != 2) {
                        return new ValidationInfo("IP:端口 格式无效", remoteTextField);
                    }
                    String ip = parts[0];
                    String port = parts[1];
                    if (!Validator.isIpv4(ip) || !port.matches("\\d+")) {
                        return new ValidationInfo("IP:端口 格式无效", remoteTextField);
                    }
                }
                return null;
            }
            default -> {
                return new ValidationInfo("请选择一个面板");
            }
        }
    }


    /**
     * 同步显示对话框的方法
     */
    public static HttpServer showAndGetSync(List<String> processList) {
        JvmProcessSelectorDialog dialog = new JvmProcessSelectorDialog(processList);
        if (dialog.showAndGet()) {
            return dialog.getSelectedValue();
        } else {
            return null;
        }
    }
}