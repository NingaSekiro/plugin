package org.aopbuddy.plugin.infra.util;

import com.aopbuddy.infrastructure.api.JsonUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.List;

public class HttpRequestUtil {
    public static String getQueryParameter(QueryStringDecoder queryStringDecoder, String paramName) {
        // 获取所有查询参数
        List<String> paramValues = queryStringDecoder.parameters().get(paramName);

        // 如果参数存在且有值，返回第一个值
        if (paramValues != null && !paramValues.isEmpty()) {
            return paramValues.get(0);
        }

        return null; // 参数不存在时返回null
    }

    /**
     * 从FullHttpRequest中获取请求体内容
     */
    public static String getRequestBody(FullHttpRequest request) {
        // 检查请求是否有内容
        if (request != null && request.content().readableBytes() > 0) {
            // 读取请求体内容并转换为字符串
            byte[] bytes = new byte[request.content().readableBytes()];
            request.content().readBytes(bytes);
            return new String(bytes);
        }
        return null;
    }

    /**
     * 从POST请求中解析JSON数据到指定类型
     */
    public static <T> T parseJsonBody(FullHttpRequest request, Class<T> clazz) {
        String body = getRequestBody(request);
        if (body != null) {
            try {
                return JsonUtil.parse(body, clazz);
            } catch (Exception e) {
                // 处理JSON解析异常
                e.printStackTrace();
            }
        }
        return null;
    }
}
