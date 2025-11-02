package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.JsonUtil;
import com.intellij.openapi.project.Project;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.infra.util.JavaPackageCollector;
import org.aopbuddy.plugin.infra.util.ProjectUtil;

import java.io.IOException;
import java.util.Set;

public class PackageNameServlet implements RouteHandler {
    @Override
    public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException {
        String projectId = HttpRequestUtil.getQueryParameter(queryStringDecoder, "projectId");
        Project project = ProjectUtil.getProjectById(projectId);
        if (project == null) {
            return "Project not found";
        }
        Set<String> packageNames = JavaPackageCollector.collectAllJavaPackages(project);
        return JsonUtil.toJson(packageNames);
    }
}
