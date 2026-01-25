package org.aopbuddy.plugin.service;

import com.aopbuddy.infrastructure.api.CallRecordDo;
import com.aopbuddy.infrastructure.api.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.aopbuddy.plugin.infra.util.BalloonTipUtil;
import org.aopbuddy.plugin.infra.util.DatabaseUtils;
import org.aopbuddy.plugin.infra.util.OkHttpClientUtils;

@Service(Service.Level.PROJECT)
public final class WebSocketClientService {

  private static final Logger LOGGER = Logger.getInstance(WebSocketClientService.class);

  private final Project project;
  private final ConsoleStateService consoleStateService;
  private final DatabaseService databaseService;

  private final OkHttpClient okHttpClient = OkHttpClientUtils.getInstance();
  private volatile WebSocket webSocket;
  private final AtomicBoolean connected = new AtomicBoolean(false);
  private final Set<String> watchedMethodKeys = ConcurrentHashMap.newKeySet();

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

  public void sendGetWatchedRequest() {
    ensureConnected();
    Map<String, Object> payload = new HashMap<>();
    payload.put("type", "get_watched_request");
    payload.put("projectId", project.getLocationHash());
    String json = JsonUtil.toJson(payload);
    webSocket.send(json);
  }

  public Set<String> getWatchedMethodKeys() {
    return watchedMethodKeys;
  }

  public void clearWatchedMethodKeys() {
    watchedMethodKeys.clear();
    refreshHighlighters();
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
      // 连接建立后自动查询当前watch状态
      sendGetWatchedRequest();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      try {
        Map<String, Object> msg = JsonUtil.parse(text, new TypeReference<Map<String, Object>>() {
        });
        Object typeObj = msg.get("type");
        String type = typeObj == null ? "" : String.valueOf(typeObj);
        if ("records".equals(type)) {
          String table = String.valueOf(msg.getOrDefault("table",
              DatabaseUtils.safeTableName(project.getName()) + "_watch"));
          String payloadJson = JsonUtil.toJson(msg.get("payload"));
          List<CallRecordDo> records = JsonUtil.parse(payloadJson,
              new TypeReference<List<CallRecordDo>>() {
              });
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

    if ("get_watched_request".equals(requestType)) {
      // 处理返回的watchedMethodKeys
      Object watchedKeysObj = msg.get("watchedMethodKeys");
      if (watchedKeysObj != null) {
        watchedMethodKeys.clear();
        if (watchedKeysObj instanceof List) {
          List<?> keysList = (List<?>) watchedKeysObj;
          for (Object key : keysList) {
            if (key != null) {
              watchedMethodKeys.add(String.valueOf(key));
            }
          }
        }
        refreshHighlighters();
      }
    } else {
      Object classNameObj = msg.get("className");
      Object methodNameObj = msg.get("methodName");
      if (classNameObj == null || methodNameObj == null) {
        return;
      }
      String methodKey = String.valueOf(classNameObj) + "#" + String.valueOf(methodNameObj);
      if ("watch_request".equals(requestType)) {
        watchedMethodKeys.add(methodKey);
        refreshHighlighters();
      } else if ("unwatch_request".equals(requestType)) {
        watchedMethodKeys.remove(methodKey);
        refreshHighlighters();
      }
    }
  }

  private void refreshHighlighters() {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (project.isDisposed()) {
        return;
      }
      FileEditorManager editorManager = FileEditorManager.getInstance(project);
      PsiManager psiManager = PsiManager.getInstance(project);
      DaemonCodeAnalyzer analyzer = DaemonCodeAnalyzer.getInstance(project);

      for (VirtualFile file : editorManager.getOpenFiles()) {
        PsiFile psiFile = psiManager.findFile(file);
        if (psiFile != null) {
          analyzer.restart(psiFile);
        }
      }
    });
  }
}
