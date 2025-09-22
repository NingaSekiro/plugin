package org.aopbuddy.plugin1.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.aopbuddy.plugin1.infra.config.ServerConfig;
import org.aopbuddy.plugin1.infra.model.EvalRequest;
import org.aopbuddy.plugin1.infra.model.HttpServer;
import org.aopbuddy.plugin1.infra.util.PluginPathUtil;
import org.aopbuddy.plugin1.infra.util.PortUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class JvmService {
    private final Project project;

    private final ConsoleStateService consoleStateService;

    public JvmService(Project project) {
        this.project = project;
        this.consoleStateService = project.getService(ConsoleStateService.class);
    }

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

    public void attach(String serverName) {
        try {
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
        } catch (AttachNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (AgentInitializationException e) {
            throw new RuntimeException(e);
        } catch (AgentLoadException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getClassloaders() {
        HttpServer server = ServerConfig.getInstance().getServerMap().get(consoleStateService.getServerName());
        String result1 = HttpUtil.get("http://" + server.getIp() + ":" + server.getPort() + "/classloaders");
        return JSONUtil.toList(result1, String.class);
    }

    public String eval(String script) {
        String serverName = consoleStateService.getServerName();
        EvalRequest evalRequest = new EvalRequest(serverName, consoleStateService.getSelectedClassloader(), script);
        HttpServer server = ServerConfig.getInstance().getServerMap().get(serverName);
        return HttpUtil.post("http://" + server.getIp() + ":" + server.getPort() + "/eval", JSONUtil.toJsonStr(evalRequest));
    }
}
