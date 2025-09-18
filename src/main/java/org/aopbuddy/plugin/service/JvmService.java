package org.aopbuddy.plugin.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.server.SimpleServer;
import cn.hutool.json.JSONUtil;
import com.sun.tools.attach.*;
import org.aopbuddy.plugin.infra.config.ServerConfig;
import org.aopbuddy.plugin.infra.model.EvalRequest;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.infra.util.PluginPathUtil;
import org.aopbuddy.plugin.infra.util.PortUtil;
import org.aopbuddy.plugin.servlet.ListenerServlet;

import javax.sound.sampled.Port;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JvmService {
    private SimpleServer simpleServer;

    public List<String> getJvms() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        List<String> processList = new ArrayList<>();

        // 构造进程信息列表
        for (VirtualMachineDescriptor vm : vms) {
            String displayName = vm.displayName();
            String id = vm.id();
            // 格式化显示信息：PID - 进程名
            String displayText = id + " - " + (displayName.isEmpty() ? "Unknown" : displayName);
            processList.add(displayText);
        }
        return processList;
    }

    public void attach(String serverName) throws AgentLoadException, IOException, AgentInitializationException, AttachNotSupportedException {
        String pid = serverName.split(" - ")[0];
        VirtualMachine vm = VirtualMachine.attach(pid);
        File pluginDir = PluginPathUtil.getPluginDirectory();
        int freePort = PortUtil.findFreePort();
        try {
            vm.loadAgent(pluginDir.getAbsolutePath() + File.separator + "lib" + File.separator + "agent-jar-with-dependencies.jar", String.valueOf(freePort));
        } catch (AgentLoadException e) {
            if (!"0".equals(e.getMessage())) {
                throw e;
            }
        }
        ServerConfig instance = ServerConfig.getInstance();
        instance.getServerMap().put(serverName, new HttpServer("127.0.0.1", freePort));
        vm.detach();
    }

    public List<String> getClassloaders(String serverName) {
        HttpServer server = ServerConfig.getInstance().getServerMap().get(serverName);
        String result1 = HttpUtil.get("http://" + server.getIp() + ":" + server.getPort() + "/classloaders");
        return JSONUtil.toList(result1, String.class);
    }

    public String eval(String serverName, EvalRequest evalRequest) {
        HttpServer server = ServerConfig.getInstance().getServerMap().get(serverName);
        return HttpUtil.post("http://" + server.getIp() + ":" + server.getPort() + "/eval", JSONUtil.toJsonStr(evalRequest));
    }


    public void startServer() {
        if (this.simpleServer != null) {
            return;
        }
        SimpleServer simpleServer = HttpUtil.createServer(8800)
                .addAction("/listener", new ListenerServlet());
        simpleServer
                .start();
        this.simpleServer = simpleServer;
    }

}
