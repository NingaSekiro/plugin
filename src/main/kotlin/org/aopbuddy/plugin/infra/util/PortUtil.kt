package org.aopbuddy.plugin.infra.util

import java.io.IOException
import java.net.ServerSocket

object PortUtil {
    fun findFreePort(): Int {
        // 从8888到8900依次遍历寻找可用端口
        for (port in 8888..8900) {
            try {
                ServerSocket(port).use { serverSocket ->
                    // 设置端口快速重用
                    serverSocket.reuseAddress = true
                    // 如果能成功创建ServerSocket，说明端口可用
                    return port
                }
            } catch (e: IOException) {
                // 端口被占用，继续尝试下一个端口
                continue
            }
        }
        // 如果8888-8900端口都被占用，抛出异常
        throw RuntimeException("Failed to find a free port in range 8888-8900")
    }
}