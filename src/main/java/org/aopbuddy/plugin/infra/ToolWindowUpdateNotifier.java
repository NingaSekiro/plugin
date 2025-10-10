package org.aopbuddy.plugin.infra;

import com.intellij.util.messages.Topic;

public interface ToolWindowUpdateNotifier {
    @Topic.ProjectLevel
    Topic<ToolWindowUpdateNotifier> GROOVY_CONSOLE_CHANGED_TOPIC = Topic.create("groovy console changed", ToolWindowUpdateNotifier.class);

    @Topic.ProjectLevel
    Topic<ToolWindowUpdateNotifier> ATTACH_STATUS_CHANGED_TOPIC = Topic.create("attach status changed", ToolWindowUpdateNotifier.class);

    void onUpdate(String newContent);  // 自定义事件方法，传递数据
}