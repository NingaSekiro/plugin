package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;
import lombok.Data;

@Service(Level.PROJECT)
@Data
public final class ConsoleStateService {
    private final Project project;
    private String serverName;
    private String ip;
    private int port;
    private String selectedClassloader;
    public ConsoleStateService(Project project) {
        this.project = project;
    }
}