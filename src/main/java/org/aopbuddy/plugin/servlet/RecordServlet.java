package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.api.CallRecordDo;
import com.aopbuddy.infrastructure.api.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.util.List;
import org.aopbuddy.plugin.infra.model.MethodChainVo;
import org.aopbuddy.plugin.infra.model.RecordRequest;
import org.aopbuddy.plugin.infra.model.RecordResp;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.infra.util.ProjectUtil;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;
import org.aopbuddy.plugin.service.DbSyncService;
import org.aopbuddy.plugin.service.HeartBeatService;

public class RecordServlet implements RouteHandler {
    private final DatabaseService databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    private DbSyncService dbSyncService;

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        RecordRequest recordRequest = HttpRequestUtil.parseJsonBody(request, RecordRequest.class);
        Project project = ProjectUtil.getProjectById(recordRequest.getProjectId());
        if (project == null) {
            RecordResp recordResp = new RecordResp();
            recordResp.setCode(-1);
            return JsonUtil.toJson(recordResp);
        }
        HeartBeatService service = project.getService(HeartBeatService.class);
        if (!service.isStatus()) {
            RecordResp recordResp = new RecordResp();
            recordResp.setCode(-1);
            return JsonUtil.toJson(recordResp);
        }
        dbSyncService = project.getService(DbSyncService.class);
        List<String> config = recordRequest.getConfig();
        if (recordRequest.isStart()) {
            if (dbSyncService.isRunning()) {
                List<CallRecordDo> callRecordDos = databaseService.execute(CallRecordMapper.class, mapper -> {
                    return mapper.selectMaxIdMethodsPerChain(dbSyncService.getTableName());
                });
                List<MethodChainVo> methodChainVos = callRecordDos.stream().map(MethodChainVo::toMethodChain).toList();
                RecordResp recordResp = new RecordResp();
                recordResp.setCode(0);
                recordResp.setMethodChains(methodChainVos);
                recordResp.setRecord(dbSyncService.getTableName());
                return JsonUtil.toJson(recordResp);
            } else {
                dbSyncService.record(config, "*");
            }
        } else {
            dbSyncService.stop();
        }
        RecordResp recordResp = new RecordResp();
        recordResp.setCode(0);
        return JsonUtil.toJson(recordResp);
    }
}
