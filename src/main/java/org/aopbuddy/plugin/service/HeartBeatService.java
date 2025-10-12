package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;
import org.aopbuddy.plugin.infra.util.ThreadUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service(Service.Level.PROJECT)
public final class HeartBeatService {
    @Setter
    private boolean isRunning = false;

    @Getter
    private boolean status = false;
    private Project project;
    private final JvmService jvmService;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public HeartBeatService(Project project) {
        this.project = project;
        jvmService = project.getService(JvmService.class);
    }

    public void heartBeat() {
        // 异步执行
        executor.submit(() -> {
            while (isRunning) {
                List<String> classloaders;
                classloaders = jvmService.getClassloaders();
                boolean attached = !classloaders.isEmpty();
                if (attached != status) {
                    status = attached;
                    ToolWindowUpdateNotifier publisher = project.getMessageBus()
                            .syncPublisher(ToolWindowUpdateNotifier.ATTACH_STATUS_CHANGED_TOPIC);
                    publisher.onUpdate(Boolean.toString(status));
                }
                ThreadUtil.sleep(4000);
            }
        });
    }

    // 当服务被销毁时调用此方法
    public void dispose() {
        isRunning = false;
    }
}
