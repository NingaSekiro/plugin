package org.aopbuddy.plugin.infra.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;

import java.io.File;
import java.nio.file.Path;

public class PluginPathUtil {

    /**
     * 获取当前插件的安装目录
     * @return 插件目录的File对象（可能为null，需处理）
     */
    public static File getPluginDirectory() {
        // 1. 获取当前插件的ID（需替换为你的插件在plugin.xml中定义的id）
        PluginId pluginId = PluginId.getId("org.aopbuddy.plugin");
        
        // 2. 通过ID获取插件描述符
        PluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(pluginId);
        if (pluginDescriptor == null) {
            // 插件未找到（通常在开发调试时不会出现，除非ID错误）
            return null;
        }
        
        // 3. 从描述符中获取插件路径（VirtualFile类型）
        Path pluginPath = pluginDescriptor.getPluginPath();
        if (pluginPath == null) {
            return null;
        }
        
        // 4. 转换为本地文件路径
        return pluginPath.toFile();
    }

    public static Path getPluginPath() {
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("org.aopbuddy.plugin"));
        Path pluginPath = pluginDescriptor.getPluginPath();
        return pluginPath;
    }
}