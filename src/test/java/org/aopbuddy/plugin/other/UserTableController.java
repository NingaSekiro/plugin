package org.aopbuddy.plugin.other;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

class UserTableController {
    private final UserTableModel model;
    private final JTable view;

    public UserTableController(UserTableModel model, JTable view) {
        this.model = model;
        this.view = view;
        // Observer: 监听 Model 变化
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                System.out.println("数据已更新: " + e.getFirstRow() + " 行变化");
            }
        });
    }

    public void onAddUser() {
        model.addUser(new User(model.getRowCount() + 1, "New User", "new@example.com"));
    }

    public void onDeleteUser() {
        int selectedRow = view.getSelectedRow();
        model.removeUser(selectedRow);
    }
}