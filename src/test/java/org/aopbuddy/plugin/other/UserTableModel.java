package org.aopbuddy.plugin.other;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class UserTableModel extends AbstractTableModel {
    private final List<User> users = new ArrayList<>();
    private final String[] columns = {"ID", "Name", "Email"};

    public UserTableModel() {
        // 初始数据
        users.add(new User(1, "Alice", "alice@example.com"));
        users.add(new User(2, "Bob", "bob@example.com"));
        fireTableDataChanged();  // 通知 View
    }

    @Override
    public int getRowCount() { return users.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public Object getValueAt(int row, int col) {
        User user = users.get(row);
        return switch (col) {
            case 0 -> user.id;
            case 1 -> user.name;
            case 2 -> user.email;
            default -> null;
        };
    }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    // Model 更新方法
    public void addUser(User user) {
        users.add(user);
        fireTableRowsInserted(users.size() - 1, users.size() - 1);
    }

    public void removeUser(int row) {
        if (row >= 0 && row < users.size()) {
            users.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
}