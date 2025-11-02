package org.aopbuddy.plugin.service;

import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.servlet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;
import org.jetbrains.io.FileResponses;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpServer extends RestService {
    public Map<String, RouteHandler> routeHandlerMap = new HashMap<>();

    public HttpServer() {
        this.routeHandlerMap.put("methodRecords", new MethodRecordsServlet());
        this.routeHandlerMap.put("methodChains", new MethodChainsServlet());
        this.routeHandlerMap.put("mermaid", new MermaidServlet());
        this.routeHandlerMap.put("record", new RecordServlet());
        this.routeHandlerMap.put("packageNames", new PackageNameServlet());
    }

    @Nullable
    @Override
    public String execute(@NotNull QueryStringDecoder queryStringDecoder, @NotNull FullHttpRequest fullHttpRequest, @NotNull ChannelHandlerContext channelHandlerContext) throws IOException {
        String path = queryStringDecoder.path();
        String servicePath = "/api/" + getServiceName() + "/";
        String resourcePath = path.substring(servicePath.length());
        if (resourcePath.contains("index.html") || resourcePath.contains("assets")) {
            handleStaticResource(channelHandlerContext, fullHttpRequest, resourcePath);
        } else {
            String json = routeHandlerMap.get(resourcePath).handle(queryStringDecoder, fullHttpRequest, channelHandlerContext);
            sendJson(channelHandlerContext, fullHttpRequest, json);
        }
        return null;
    }

    private void handleStaticResource(ChannelHandlerContext context, FullHttpRequest request, String path) {
        try {
            Path pathTmp = Path.of("D:\\Vue\\mermaid\\dist\\" + path);
            FileResponses.INSTANCE.sendFile(request, context.channel(), pathTmp, EmptyHttpHeaders.INSTANCE);
        } catch (Exception e) {
            LOG.error("Error handling static resource: " + path, e);
        }
    }

    private void sendJson(ChannelHandlerContext context, FullHttpRequest request, String json) throws IOException {
        BufferExposingByteArrayOutputStream out = new BufferExposingByteArrayOutputStream();
        out.write(json.getBytes(StandardCharsets.UTF_8));
        send(
                out,                 // 响应体数据
                request,             // 原始请求
                context            // Netty context
        );
    }

    @NotNull
    @Override
    protected String getServiceName() {
        return "aopPlugin";
    }

    @Override
    protected boolean isHostTrusted(@NotNull FullHttpRequest request) {
        return true;
    }

    @Override
    protected boolean isMethodSupported(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.POST;
    }
}
