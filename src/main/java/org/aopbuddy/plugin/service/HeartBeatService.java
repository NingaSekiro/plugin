package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier;

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
                boolean attached = classloaders.size() > 0;
                if (attached != status) {
                    status = attached;
                    ToolWindowUpdateNotifier publisher = project.getMessageBus()
                            .syncPublisher(ToolWindowUpdateNotifier.ATTACH_STATUS_CHANGED_TOPIC);
                    publisher.onUpdate(String.valueOf(status));
                }
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // 当服务被销毁时调用此方法
    public void dispose() {
        isRunning = false;
    }
}
