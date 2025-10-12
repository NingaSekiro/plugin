package org.aopbuddy.plugin.infra.util;

import okhttp3.OkHttpClient;
import org.aopbuddy.plugin.infra.OkHttpRetryInterceptor;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * 程序中尽可能使用单例 OkHttpClient，也就是多个 HTTP 请求尽可能使用同一个 OkHttpClient 变量，
 * 屡次 new OkHttpClient 会致使抛出 too many ope files 的异常，这个异常是由于多个 OkHttpClient
 * 链接了多个 socket 致使的。如果有需要，可以使用 OkHttpClient 的 newBuilder() 方法，再进行自定义。
 */
public final class OkHttpClientUtils {

    private OkHttpClientUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取<code>OkHttpClient</code>单例
     *
     * @return <code>OkHttpClient</code>单例
     */
    public static OkHttpClient getInstance() {
        return OkHttpClientHolder.INSTANCE;
    }

    private static class OkHttpClientHolder {
        private static final OkHttpClient INSTANCE = new OkHttpClient
                .Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(new OkHttpRetryInterceptor(3))
                .proxy(Proxy.NO_PROXY)
                .retryOnConnectionFailure(false)
                .build();
    }
}