package org.aopbuddy.plugin.other;

import javax.swing.*;

public class SwingMVCDemo extends JFrame {
    private final JTable table;
    private final UserTableController controller;

    public SwingMVCDemo() {
        setTitle("Swing MVC 示例：用户表格");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new java.awt.BorderLayout());

        // Model
        UserTableModel model = new UserTableModel();

        // View
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加用户");
        JButton deleteButton = new JButton("删除选中行");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        add(scrollPane, java.awt.BorderLayout.CENTER);
        add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // Controller 绑定
        controller = new UserTableController(model, table);
        addButton.addActionListener(e -> controller.onAddUser());
        deleteButton.addActionListener(e -> controller.onDeleteUser());

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingMVCDemo::new);
    }
}