package org.aopbuddy.plugin1.infra.util;

import java.io.IOException;
import java.net.ServerSocket;

public class PortUtil {
    public static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            // 绑定到端口 0，系统会自动分配一个空闲端口
//            当一个服务器程序（如 ServerSocket）绑定到一个端口并运行一段时间后关闭，操作系统不会立即释放该端口。端口可能会进入 TIME_WAIT 状态（通常持续 30 秒到 2 分钟，具体取决于操作系统）
//            设置 setReuseAddress(true) 允许 ServerSocket 绑定到这个端口
            serverSocket.setReuseAddress(true); // 允许端口快速重用
            return serverSocket.getLocalPort(); // 获取分配的端口
        } catch (IOException e) {
            throw new RuntimeException("Failed to find a free port", e);
        }
    }
}
