package org.aopbuddy.plugin.service;

import com.aopbuddy.infrastructure.JsonUtil;
import com.aopbuddy.infrastructure.MethodChainUtil;
import com.aopbuddy.record.CallRecordDo;
import com.aopbuddy.record.MethodChain;
import com.aopbuddy.record.MethodChainKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.aopbuddy.plugin.infra.util.DatabaseUtils;
import org.aopbuddy.plugin.infra.util.ThreadUtil;
import org.aopbuddy.plugin.mapper.CallRecordMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class DbSyncService {
    private static final Logger LOGGER = Logger.getInstance(DbSyncService.class);

    @Getter
    @Setter
    private boolean isRunning = false;
    @Getter
    private String tableName = "";
    private Project project;
    private final DatabaseService databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    private final JvmService jvmService;
    private long startTime = 0;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);


    public DbSyncService(Project project) {
        this.project = project;
        jvmService = project.getService(JvmService.class);
    }
    // client 0->server0-?->
    // client ?->server?-??

    public void reInit() {
        isRunning = false;
    }

    // 定时任务 - 实际的同步逻辑
    public void record(List<String> classNames, String methodName) {
        isRunning = true;
        tableName = DatabaseUtils.safeTableName(project.getName()) + "_" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"));
        executor.submit(() -> {
            // 开始录制
            String tmp = jvmService.eval("return System.currentTimeMillis()");
            startTime = Long.parseLong(tmp);
            StringBuilder stringBuilder = new StringBuilder();
            for (String className : classNames) {
                stringBuilder.append(String.format("addListener('%s..*','%s')\n",
                        className,
                        methodName));
            }
            String addListenerResult = jvmService.eval(stringBuilder.toString());
            LOGGER.info(String.format("register trace listener result: %s", addListenerResult));
            databaseService.execute(CallRecordMapper.class, mapper -> {
                mapper.createTableWithName(tableName);
                return null;
            });
        });
        // 异步执行
        executor.submit(() -> {
            while (isRunning) {
                try {
                    String eval = jvmService.eval("syncDb(" + startTime + ")");
                    if (eval == null || "{}".equals(eval)) {
                        continue;
                    }
                    Map<MethodChainKey, MethodChain> map = JsonUtil.parse(eval, new TypeReference<Map<MethodChainKey, MethodChain>>() {
                    });
                    databaseService.execute(CallRecordMapper.class, mapper -> {
                        List<CallRecordDo> callRecords = map.values().stream()
                                .flatMap(m -> m.getCallRecordDos().stream()
                                        .flatMap(c -> c.getCallRecords().stream()))
                                .collect(Collectors.toList());
                        if (callRecords.isEmpty()) {
                            return null;
                        }
                        int i = mapper.insertBatchCallRecords(callRecords, tableName);
                        if (i != callRecords.size()) {
                            LOGGER.warn(String.format("insert table %s fail", tableName));
                        }
                        startTime = MethodChainUtil.getMaxTime(map);
                        LOGGER.info(String.format("insert table %s success", tableName));
                        return null;
                    });
                } catch (Throwable e) {
                    LOGGER.error(e);
                } finally {
                    ThreadUtil.sleep(3000);
                }
            }
        });
    }

    public void stop() {
        isRunning = false;
        // 停止录制
        String eval = jvmService.eval("deleteListener()");
        LOGGER.info(String.format("delete trace listener result: %s", eval));
        LOGGER.info("stop sync db");
    }
}

