package org.aopbuddy.plugin.infra

import com.intellij.util.messages.Topic

fun interface ToolWindowUpdateNotifier {
    companion object {
        @Topic.ProjectLevel
        val UPDATE_TOPIC: Topic<ToolWindowUpdateNotifier> = Topic.create("tool window update", ToolWindowUpdateNotifier::class.java)
    }

    fun onUpdate(newContent: String)  // 自定义事件方法，传递数据
}