package org.aopbuddy.plugin.toolwindow.model;

import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.service.ConsoleStateService;
import org.aopbuddy.plugin.service.HeartBeatService;
import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.StatusChangeListener;

import java.util.ArrayList;
import java.util.List;

public class AttachModel {
    private final JvmService jvmService;
    private final HeartBeatService heartBeatService;
    private final ConsoleStateService consoleStateService;
    private final List<StatusChangeListener<Boolean>> listeners = new ArrayList<>();
    private boolean isAttached = false;

    public AttachModel(JvmService jvmService, HeartBeatService heartBeatService, ConsoleStateService consoleStateService) {
        this.jvmService = jvmService;
        this.heartBeatService = heartBeatService;
        this.consoleStateService = consoleStateService;
    }

    public List<String> getJvms() {
        return jvmService.getJvms();
    }

    public void startHeartBeat(HttpServer httpServer) {
        if (httpServer != null) {
            consoleStateService.setServerName(httpServer.getName());
            consoleStateService.setIp(httpServer.getIp());
            consoleStateService.setPort(httpServer.getPort());
            // 本地附着
            if (httpServer.getName() != null) {
                jvmService.attach(httpServer);
            }
            // 更新连接状态标签
            heartBeatService.setRunning(true);
            heartBeatService.heartBeat();
        }
    }

    public void setStatus(Boolean status) {
        if (status == null || status.equals(isAttached)) {
            return; // 避免重复通知
        }
        isAttached = status;
        notifyListeners(); // 通知所有观察者
    }


    // 注册观察者
    public void addStatusChangeListener(StatusChangeListener<Boolean> listener) {
        listeners.add(listener);
    }

    // 移除观察者
    public void removeStatusChangeListener(StatusChangeListener<Boolean> listener) {
        listeners.remove(listener);
    }

    // 通知所有观察者状态变更
    private void notifyListeners() {
        for (StatusChangeListener<Boolean> listener : listeners) {
            listener.onStatusChanged(isAttached);
        }
    }
}
