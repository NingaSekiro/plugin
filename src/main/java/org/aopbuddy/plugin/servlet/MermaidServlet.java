package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.JsonUtil;
import com.aopbuddy.record.CallRecordDo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.infra.model.MethodVo;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)

public final class MermaidServlet implements RouteHandler {

    private final DatabaseService databaseService;


    public MermaidServlet() {
        this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    }

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        List<CallRecordDo> execute = databaseService.execute(CallRecordMapper.class, mapper -> {
            List<String> dbNames = mapper.selectAllTableNames();
            List<CallRecordDo> callRecordDos = mapper.selectMaxIdMethodsPerChain(dbNames.get(0));
            int callChainId = callRecordDos.get(0).getCallChainId();
            return mapper.selectMethodsByChainId(callChainId, dbNames.get(0));
        });
        List<MethodVo> methodVoList = execute.stream()
                .map(MethodVo::toMethod).collect(Collectors.toList());
        return JsonUtil.toJson(methodVoList);
    }
}
