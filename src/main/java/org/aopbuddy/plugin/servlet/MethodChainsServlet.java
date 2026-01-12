package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.api.CallRecordDo;
import com.aopbuddy.infrastructure.api.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.util.List;
import org.aopbuddy.plugin.infra.model.MethodChainVo;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

public class MethodChainsServlet implements RouteHandler {
    private final DatabaseService databaseService;

    public MethodChainsServlet() {
        this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    }

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        String record = HttpRequestUtil.getQueryParameter(queryStringDecoder, "record");
        List<CallRecordDo> callRecordDos = databaseService.execute(CallRecordMapper.class, mapper -> {
            return mapper.selectMaxIdMethodsPerChain(record);
        });
        List<MethodChainVo> methodChainVos = callRecordDos.stream().map(MethodChainVo::toMethodChain).toList();
        return JsonUtil.toJson(methodChainVos);
    }
}
