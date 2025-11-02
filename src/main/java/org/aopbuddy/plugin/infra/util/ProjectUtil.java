package org.aopbuddy.plugin.infra.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nullable;

/**
 * 项目工具类，提供根据projectId获取Project对象的方法
 */
public class ProjectUtil {

    /**
     * 根据projectId获取Project对象
     *
     * @param projectId 项目唯一标识符
     * @return Project对象，如果未找到则返回null
     */
    @Nullable
    public static Project getProjectById(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            return null;
        }

        // 获取所有打开的项目
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        // 遍历所有项目，查找匹配的projectId
        for (Project project : openProjects) {
            // 检查项目是否有效且projectId匹配
            if (project != null && project.isDisposed()) {
                continue;
            }


            // 方式1：通过项目名称匹配
            if (project.getLocationHash().equals(projectId)) {
                return project;
            }
        }
        return null; // 未找到匹配的项目
    }

    /**
     * 获取当前活跃的项目（如果有多个打开的项目）
     *
     * @return 当前活跃的Project对象，如果没有则返回null
     */
    @Nullable
    public static Project getActiveProject() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        return openProjects.length > 0 ? openProjects[0] : null;
    }
}
