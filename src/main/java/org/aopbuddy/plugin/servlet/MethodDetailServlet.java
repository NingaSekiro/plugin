package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.JsonUtil;
import com.aopbuddy.record.CallRecordDo;
import com.intellij.openapi.application.ApplicationManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

import java.io.IOException;

public class MethodDetailServlet implements RouteHandler {

    private final DatabaseService databaseService;


    public MethodDetailServlet() {
        this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    }

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        String id = HttpRequestUtil.getQueryParameter(queryStringDecoder, "id");
        String record = HttpRequestUtil.getQueryParameter(queryStringDecoder, "record");
        CallRecordDo callRecordDo = databaseService.execute(CallRecordMapper.class, mapper -> {
            return mapper.selectById(Long.valueOf(id), record);
        });
        return JsonUtil.toJson(callRecordDo);
    }
}
