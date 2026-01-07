package org.aopbuddy.plugin.service;

import com.aopbuddy.adapter.model.EvalRequest;
import com.aopbuddy.infrastructure.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.aopbuddy.plugin.action.HotswapAction.ClassFilePath;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.infra.util.OkHttpClientUtils;
import org.aopbuddy.plugin.infra.util.PluginPathUtil;

@Service(Service.Level.PROJECT)
public final class JvmService {

  private static final Logger LOGGER = Logger.getInstance(JvmService.class);

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
      if (displayName.contains("GradleDaemon")) {
        continue;
      }
      String id = vm.id();
      // 格式化显示信息：PID - 进程名
      String displayText = id + " - " + (displayName.isEmpty() ? "Unknown" : displayName);
      processList.add(displayText);
    }
    return processList;
  }

  public void attach(HttpServer httpServer) {
    try {
      String pid = httpServer.getName().split(" - ")[0];
      VirtualMachine vm = VirtualMachine.attach(pid);
      File pluginDir = PluginPathUtil.getPluginDirectory();
      int freePort = httpServer.getPort();
      try {
        vm.loadAgent(pluginDir.getAbsolutePath() + File.separator + "lib" + File.separator
            + "agent-jar-with-dependencies.jar", String.valueOf(freePort));
      } catch (AgentLoadException e) {
        if (!"0".equals(e.getMessage())) {
          throw e;
        }
      }
      vm.detach();
    } catch (Throwable e) {
      LOGGER.error("attach error", e);
      throw new RuntimeException(e);
    }
  }

  public List<String> getClassloaders() {
    OkHttpClient okHttpClient = OkHttpClientUtils.getInstance();
    Request request = new Request.Builder()
        .url("http://" + consoleStateService.getIp() + ":" + consoleStateService.getPort()
            + "/classloader")
        .method("GET", null)
        .build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()) {
        return new ArrayList<>();
      }
      return JsonUtil.parse(response.body().string(), new TypeReference<>() {
      });
    } catch (Throwable e) {
      LOGGER.error("getClassloaders error", e);
      return new ArrayList<>();
    }
  }

  public String eval(String script) {
    if (consoleStateService.getIp() == null) {
      return null;
    }
    EvalRequest evalRequest = new EvalRequest(consoleStateService.getServerName(),
        consoleStateService.getSelectedClassloader(), script);
    OkHttpClient okHttpClient = OkHttpClientUtils.getInstance();
    Request request = new Request.Builder()
        .url(
            "http://" + consoleStateService.getIp() + ":" + consoleStateService.getPort() + "/eval")
        .method("POST",
            RequestBody.create(JsonUtil.toJson(evalRequest), MediaType.parse("application/json")))
        .build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      return response.body().string();
    } catch (IOException e) {
      return null;
    }
  }

  public String hotSwap(List<ClassFilePath> classFilePaths) {
    if (consoleStateService.getIp() == null) {
      return null;
    }
    OkHttpClient okHttpClient = OkHttpClientUtils.getInstance();
    Request request = new Request.Builder()
        .url("http://" + consoleStateService.getIp() + ":" + consoleStateService.getPort()
            + "/hotswap")
        .method("POST", RequestBody.create(JsonUtil.toJson(classFilePaths),
            MediaType.parse("application/json")))
        .build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      return response.body().string();
    } catch (Throwable e) {
      LOGGER.error("hotSwap error", e);
      return null;
    }
  }
}
