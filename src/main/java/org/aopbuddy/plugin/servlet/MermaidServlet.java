package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.JsonUtil;
import com.aopbuddy.record.CallRecordDo;
import com.intellij.openapi.application.ApplicationManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.infra.model.MermaidVo;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.infra.util.MermaidConverter;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

import java.io.IOException;
import java.util.List;

public class MermaidServlet implements RouteHandler {
    private final DatabaseService databaseService;


    public MermaidServlet() {
        this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    }

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        String record = HttpRequestUtil.getQueryParameter(queryStringDecoder, "record");
        Integer callChainId = Integer.valueOf(HttpRequestUtil.getQueryParameter(queryStringDecoder, "callChainId"));
        List<CallRecordDo> execute = databaseService.execute(CallRecordMapper.class, mapper -> {
            return mapper.selectMethodsByChainId(callChainId, record);
        });
        String mermaidCode = MermaidConverter.convertToMermaid(execute);
        return JsonUtil.toJson(new MermaidVo(mermaidCode));
    }
}
