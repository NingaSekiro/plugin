package org.aopbuddy.plugin.toolwindow;

public interface StatusChangeListener<T> {
    void onStatusChanged(T newStatus); // 状态变更时触发
}
