package org.aopbuddy.plugin.servlet;

import com.aopbuddy.infrastructure.api.CallRecordDo;
import com.aopbuddy.infrastructure.api.JsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.List;
import org.aopbuddy.plugin.infra.model.MermaidRequest;
import org.aopbuddy.plugin.infra.model.MermaidVo;
import org.aopbuddy.plugin.infra.util.HttpRequestUtil;
import org.aopbuddy.plugin.infra.util.TreeNodeUtil;
import org.aopbuddy.plugin.mapper.CallRecordMapper;
import org.aopbuddy.plugin.service.DatabaseService;

public class MermaidServlet implements RouteHandler {

  private final DatabaseService databaseService;


  public MermaidServlet() {
    this.databaseService = ApplicationManager.getApplication().getService(DatabaseService.class);
  }

  @Override
  public String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request,
      ChannelHandlerContext context) {
    MermaidRequest mermaidRequest = HttpRequestUtil.parseJsonBody(request, MermaidRequest.class);
    List<CallRecordDo> execute = databaseService.execute(CallRecordMapper.class, mapper -> {
      return mapper.selectMethodsByChainId(mermaidRequest.getCallChainId(),
          mermaidRequest.getRecord());
    });
    MermaidVo mermaidVo = new MermaidVo(TreeNodeUtil.buildTree(execute, 0));
    return JsonUtil.toJson(mermaidVo);
  }
}
