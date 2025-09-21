package org.aopbuddy.plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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