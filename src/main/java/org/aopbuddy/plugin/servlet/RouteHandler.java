package org.aopbuddy.plugin.servlet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;

public interface RouteHandler {
    String handle(QueryStringDecoder queryStringDecoder, FullHttpRequest request, ChannelHandlerContext context) throws IOException;
}