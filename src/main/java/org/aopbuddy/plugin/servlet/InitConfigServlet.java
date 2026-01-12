package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.api.JsonUtil;
import com.intellij.openapi.project.Project;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.util.Set;
import org.aopbuddy.plugin.infra.model.ConfigResp;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.infra.util.JavaPackageCollector;
import org.aopbuddy.plugin.infra.util.ProjectUtil;
import org.aopbuddy.plugin.service.HeartBeatService;

public class InitConfigServlet implements RouteHandler {
    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        String projectId = HttpRequestUtil.getQueryParameter(queryStringDecoder, "projectId");
        Project project = ProjectUtil.getProjectById(projectId);
        if (project == null) {
            return "Project not found";
        }
        Set<String> packageNames = JavaPackageCollector.collectAllJavaPackages(project);
        ConfigResp configResp = new ConfigResp();
        configResp.setPackageNames(packageNames);
        configResp.setStatus(project.getService(HeartBeatService.class).isStatus());
        return JsonUtil.toJson(configResp);
    }
}
