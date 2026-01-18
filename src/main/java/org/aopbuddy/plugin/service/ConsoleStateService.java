package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Service(Level.PROJECT)
@Data
public final class ConsoleStateService {
    private final Project project;
    private String serverName;
    private String ip;
    private int port;
    private String selectedClassloader;
    private Set<String> watchedMethodKeys = new HashSet<>();
    public ConsoleStateService(Project project) {
        this.project = project;
    }
}
