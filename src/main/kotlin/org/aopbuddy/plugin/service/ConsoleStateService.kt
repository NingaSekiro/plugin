package org.aopbuddy.plugin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ConsoleStateService(val project: Project) {
    var serverName: String? = null
    var selectedClassloader: String? = null
    var availableClassloaders: List<String> = ArrayList()
}