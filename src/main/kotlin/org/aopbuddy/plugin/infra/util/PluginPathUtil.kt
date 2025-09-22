package org.aopbuddy.plugin.infra.util

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import java.io.File
import java.nio.file.Path

object PluginPathUtil {

    /**
     * 获取当前插件的安装目录
     * @return 插件目录的File对象（可能为null，需处理）
     */
    fun getPluginDirectory(): File? {
        // 1. 获取当前插件的ID（需替换为你的插件在plugin.xml中定义的id）
        val pluginId = PluginId.getId("org.aopbuddy.plugin")

        // 2. 通过ID获取插件描述符
        val pluginDescriptor = PluginManagerCore.getPlugin(pluginId)
            ?: // 插件未找到（通常在开发调试时不会出现，除非ID错误）
            return null

        // 3. 从描述符中获取插件路径（VirtualFile类型）
        val pluginPath: Path = pluginDescriptor.pluginPath ?: return null

        // 4. 转换为本地文件路径
        return pluginPath.toFile()
    }
}