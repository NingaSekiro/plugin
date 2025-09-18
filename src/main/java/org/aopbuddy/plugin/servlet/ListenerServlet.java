package org.aopbuddy.plugin.servlet;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import cn.hutool.http.server.action.Action;

import java.io.IOException;

public class ListenerServlet implements Action {
    @Override
    public void doAction(HttpServerRequest request, HttpServerResponse response) throws IOException {
        String body = request.getBody();
    }
}
