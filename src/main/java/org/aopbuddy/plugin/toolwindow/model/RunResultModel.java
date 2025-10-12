package org.aopbuddy.plugin.toolwindow.model;


import org.aopbuddy.plugin.toolwindow.StatusChangeListener;

import java.util.ArrayList;
import java.util.List;

public class RunResultModel {
    private String currentStatus; // 存储当前状态文本
    private final List<StatusChangeListener<String>> listeners = new ArrayList<>();

    // 设置状态（会触发通知）
    public void setStatus(String status) {
        if (status == null || status.equals(currentStatus)) {
            return; // 避免重复通知
        }
        currentStatus = status;
        notifyListeners(); // 通知所有观察者
    }

    // 获取当前状态
    public String getStatus() {
        return currentStatus;
    }

    // 注册观察者
    public void addStatusChangeListener(StatusChangeListener<String> listener) {
        listeners.add(listener);
    }

    // 移除观察者
    public void removeStatusChangeListener(StatusChangeListener<String> listener) {
        listeners.remove(listener);
    }

    // 通知所有观察者状态变更
    private void notifyListeners() {
        for (StatusChangeListener<String> listener : listeners) {
            listener.onStatusChanged(currentStatus);
        }
    }
}
