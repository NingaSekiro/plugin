package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

import java.util.List;

public class MethodRecordsServlet implements RouteHandler {

    private final DatabaseService databaseService;

    public MethodRecordsServlet() {
        this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
    }

    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) {
        List<String> dbNames = databaseService.execute(CallRecordMapper.class, mapper -> mapper.selectAllTableNames());
        return JsonUtil.toJson(dbNames);
    }
}
