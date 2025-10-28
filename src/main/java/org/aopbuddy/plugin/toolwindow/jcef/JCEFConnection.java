package org.aopbuddy.plugin.toolwindow.jcef;

import lombok.SneakyThrows;
import org.cef.misc.IntRef;
import org.cef.network.CefResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * 资源连接处理
 *
 * @author zjw
 * @date 2025-01-21
 */
public class JCEFConnection {
    private URLConnection connection;
    private InputStream inputStream;

    public JCEFConnection(URLConnection connection) throws IOException {
        this.connection = connection;
        this.inputStream = connection.getInputStream();
    }

    @SneakyThrows
    public void getResponseHeaders(CefResponse cefResponse, IntRef responseLength) {
        // 设置响应头信息
        cefResponse.setMimeType(connection.getContentType());
        responseLength.set(inputStream.available());
        cefResponse.setStatus(200);

    }

    @SneakyThrows
    public boolean readResponse(byte[] dataOut, int dataSize, IntRef bytesRead) {
        // 读取响应数据
        int available = inputStream.available();
        if (available > 0) {
            int bytesToRead = Math.min(available, dataSize);
            bytesRead.set(inputStream.read(dataOut, 0, bytesToRead));
            return true;
        }
        return false;
    }

    public void close() throws IOException {
        // 关闭流
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
