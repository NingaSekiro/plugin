package org.aopbuddy.plugin.service;

import com.aopbuddy.infrastructure.api.CallRecordDo;
import com.aopbuddy.infrastructure.api.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import okhttp3.*;
import okio.ByteString;
import org.aopbuddy.plugin.infra.util.BalloonTipUtil;
import org.aopbuddy.plugin.infra.util.DatabaseUtils;
import org.aopbuddy.plugin.infra.util.OkHttpClientUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service(Service.Level.PROJECT)
public final class WebSocketClientService {

  private static final Logger LOGGER = Logger.getInstance(WebSocketClientService.class);

  private final Project project;
  private final ConsoleStateService consoleStateService;
  private final DatabaseService databaseService;

  private final OkHttpClient okHttpClient = OkHttpClientUtils.getInstance();
  private volatile WebSocket webSocket;
  private final AtomicBoolean connected = new AtomicBoolean(false);

  public WebSocketClientService(Project project) {
    this.project = project;
    this.consoleStateService = project.getService(ConsoleStateService.class);
    this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
  }

  public boolean connect() {
    if (connected.get()) {
      return true;
    }
    String ip = consoleStateService.getIp();
    int port = consoleStateService.getPort();
    if (ip == null || port <= 0) {
      return false;
    }
    String url = "ws://" + ip + ":" + port + "/ws/watch";
    Request request = new Request.Builder().url(url).build();
    webSocket = okHttpClient.newWebSocket(request, new WsListener());
    return true;
  }

  public void close() {
    if (webSocket != null) {
      try {
        webSocket.close(1000, "client close");
      } catch (Throwable ignored) {
      } finally {
        webSocket = null;
        connected.set(false);
      }
    }
  }

  public void sendWatchRequest(String className, String methodName, String desc) {
    ensureConnected();
    Map<String, Object> payload = new HashMap<>();
    payload.put("type", "watch_request");
    payload.put("projectId", project.getLocationHash());
    payload.put("className", className);
    payload.put("methodName", methodName);
    payload.put("desc", desc);
    payload.put("classloaderHint", consoleStateService.getSelectedClassloader());
    String json = JsonUtil.toJson(payload);
    webSocket.send(json);
  }

  public void sendUnwatchRequest(String className, String methodName) {
    ensureConnected();
    Map<String, Object> payload = new HashMap<>();
    payload.put("type", "unwatch_request");
    payload.put("projectId", project.getLocationHash());
    payload.put("className", className);
    payload.put("methodName", methodName);
    String json = JsonUtil.toJson(payload);
    webSocket.send(json);
  }

  private void ensureConnected() {
    if (!connected.get()) {
      connect();
    }
  }

  private final class WsListener extends WebSocketListener {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
      connected.set(true);
      LOGGER.info("WebSocket connected: " + response.message());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      try {
        Map<String, Object> msg = JsonUtil.parse(text, new TypeReference<Map<String, Object>>() {});
        Object typeObj = msg.get("type");
        String type = typeObj == null ? "" : String.valueOf(typeObj);
        if ("records".equals(type)) {
          String table = String.valueOf(msg.getOrDefault("table",
              DatabaseUtils.safeTableName(project.getName()) + "_watch"));
          String payloadJson = JsonUtil.toJson(msg.get("payload"));
          List<CallRecordDo> records = JsonUtil.parse(payloadJson, new TypeReference<List<CallRecordDo>>() {});
          if (records != null && !records.isEmpty()) {
            databaseService.execute(
                org.aopbuddy.plugin.mapper.CallRecordMapper.class,
                mapper -> {
                  mapper.createTableWithName(table);
                  mapper.insertBatchCallRecords(records, table);
                  return null;
                }
            );
          }
        } else if ("ack".equals(type)) {
          handleAck(msg);
        } else if ("error".equals(type)) {
          BalloonTipUtil.notifyError(project, "Watch失败: " + text);
          LOGGER.warn("Watch ERROR: " + text);
        }
      } catch (Throwable e) {
        LOGGER.error("ws onMessage error", e);
      }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
      onMessage(webSocket, bytes.utf8());
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
      connected.set(false);
      LOGGER.info("WebSocket closed: " + code + ", " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
      connected.set(false);
      LOGGER.warn("WebSocket failure", t);
    }
  }

  private void handleAck(Map<String, Object> msg) {
    Object requestTypeObj = msg.get("requestType");
    String requestType = requestTypeObj == null ? "" : String.valueOf(requestTypeObj);
    Object okObj = msg.get("ok");
    boolean ok = okObj != null && Boolean.parseBoolean(String.valueOf(okObj));
    if (!ok) {
      return;
    }
    Object classNameObj = msg.get("className");
    Object methodNameObj = msg.get("methodName");
    if (classNameObj == null || methodNameObj == null) {
      return;
    }
    String methodKey = String.valueOf(classNameObj) + "#" + String.valueOf(methodNameObj);
    if ("watch_request".equals(requestType)) {
      consoleStateService.getWatchedMethodKeys().add(methodKey);
      DaemonCodeAnalyzer.getInstance(project).restart();
    } else if ("unwatch_request".equals(requestType)) {
      consoleStateService.getWatchedMethodKeys().remove(methodKey);
      DaemonCodeAnalyzer.getInstance(project).restart();
    }
  }
}
