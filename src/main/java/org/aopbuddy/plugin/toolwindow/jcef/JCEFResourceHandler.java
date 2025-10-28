package org.aopbuddy.plugin.toolwindow.jcef;

import lombok.SneakyThrows;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.net.URL;

/**
 * 资源处理
 *
 * @author zjw
 * @date 2025-01-21
 */
public class JCEFResourceHandler implements CefResourceHandler {
    private JCEFConnection connection = null;

    @Override
    public boolean processRequest(CefRequest cefRequest, CefCallback callback) {
        String url = cefRequest.getURL();
        // 拦截本地文件请求并处理
        if (url.startsWith("http://butterfly")) {
            URL resource = getClass().getClassLoader().getResource(url.replace("http://butterfly", "static"));
            if (resource == null) {
                return false;
            }
            try {
                connection = new JCEFConnection(resource.toURI().toURL().openConnection());
                callback.Continue();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void getResponseHeaders(CefResponse cefResponse, IntRef responseLength, StringRef redirectUrl) {
        if (connection != null) {
            connection.getResponseHeaders(cefResponse, responseLength);
        }
    }

    @Override
    public boolean readResponse(byte[] dataOut, int dataSize, IntRef bytesRead, CefCallback callback) {
        if (connection != null) {
            return connection.readResponse(dataOut, dataSize, bytesRead);
        }
        return false;
    }

    @SneakyThrows
    @Override
    public void cancel() {
        if (connection != null) {
            connection.close();
        }
    }
}
