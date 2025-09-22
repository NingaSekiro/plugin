package org.aopbuddy.plugin1.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Service(Level.PROJECT)
@Data
public final class ConsoleStateService {
    private final Project project;
    private String serverName;
    private String selectedClassloader;
    private List<String> availableClassloaders = new ArrayList<>();

    public ConsoleStateService(Project project) {
        this.project = project;
    }
}