package org.aopbuddy.plugin.toolwindow.model;

import org.aopbuddy.plugin.service.ConsoleStateService;
import org.aopbuddy.plugin.service.DbSyncService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordModel {
    private long startTime;
    private long endTime;
    private String listenerClassName;
    private String listenerMethodName;

    // 依赖服务
    private ConsoleStateService consoleStateService;
    private DbSyncService dbSyncService;

    public RecordModel(ConsoleStateService consoleStateService, DbSyncService dbSyncService) {
        this.consoleStateService = consoleStateService;
        this.dbSyncService = dbSyncService;
    }

    public void startRecording(String className, String methodName) {
        startTime = System.currentTimeMillis();
        listenerClassName = className.trim();
        listenerMethodName = methodName.trim();
//        dbSyncService.record(listenerClassName, listenerMethodName);
    }

    public void stopRecording() {
        endTime = System.currentTimeMillis();
//        dbSyncService.stop(listenerClassName, listenerMethodName);
    }

    public boolean isAttached() {
        return consoleStateService.getIp() != null;
    }

    public String getFormattedStartTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date(startTime));
    }

    public String getRecordingDuration() {
        long duration = endTime - startTime;
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        return String.format("%d分%d秒", minutes, seconds);
    }
}