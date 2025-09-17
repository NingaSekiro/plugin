package org.aopbuddy.plugin.infra;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface ToolWindowUpdateNotifier {
    @Topic.ProjectLevel
    Topic<ToolWindowUpdateNotifier> UPDATE_TOPIC = Topic.create("tool window update", ToolWindowUpdateNotifier.class);

    void onUpdate(String newContent);  // 自定义事件方法，传递数据
}