package org.aopbuddy.plugin.toolwindow.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddMethodBreakpointFrame extends JFrame {
    // 文本框，用于输入类模式
    private JTextField classPatternField;
    // 文本框，用于输入方法名
    private JTextField methodNameField;

    // 定义对话框结果枚举
    public enum DialogResult {
        OK, CANCEL
    }

    private DialogResultCallback dialogResultCallback;

    // 定义回调接口
    public interface DialogResultCallback {
        void onResult(DialogResult result, String classPattern, String methodName);
    }

    public AddMethodBreakpointFrame(DialogResultCallback dialogResultCallback) {
        this.dialogResultCallback = dialogResultCallback;
        // 设置窗口标题
        setTitle("Add Method Breakpoint");
        // 设置窗口大小
        setSize(400, 250);
        // 设置窗口关闭时的操作：退出程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 使用 BorderLayout 布局管理器
        setLayout(new BorderLayout(10, 10));

        // 创建一个面板，用于放置类模式相关的标签和文本框
        JPanel classPatternPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel classPatternLabel = new JLabel("Class pattern:");
        classPatternField = new JTextField(20);
        // 初始化类模式文本框内容
        classPatternField.setText("com.");
        classPatternPanel.add(classPatternLabel);
        classPatternPanel.add(classPatternField);

        // 创建一个面板，用于放置方法名相关的标签和文本框
        JPanel methodNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel methodNameLabel = new JLabel("Method name:");
        methodNameField = new JTextField(20);
        methodNamePanel.add(methodNameLabel);
        methodNamePanel.add(methodNameField);

        // 创建一个面板，用于放置按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // 为 OK 按钮添加点击事件监听器
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取类模式和方法名的输入内容
                String classPattern = classPatternField.getText();
                String methodName = methodNameField.getText();
                // 调用回调函数传递结果
                dialogResultCallback.onResult(DialogResult.OK, classPattern, methodName);
                // 关闭窗口
                dispose();
            }
        });

        // 为 Cancel 按钮添加点击事件监听器，点击后关闭窗口
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogResultCallback.onResult(DialogResult.CANCEL, null, null);
                dispose();
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // 将各个面板添加到主窗口的不同区域
        add(classPatternPanel, BorderLayout.NORTH);
        add(methodNamePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 设置窗口可见
        setVisible(true);
    }

}