package org.aopbuddy.plugin.infra;

import io.netty.handler.timeout.ReadTimeoutException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.conn.ConnectTimeoutException;

/**
 * OkHttp重试拦截器<br>
 * 使用：
 * <pre>
 * OkHttpClient client = new OkHttpClient
 *        .Builder()
 *        .connectTimeout(okhttpTimeOut, TimeUnit.SECONDS)      // 设置超时时长，单位秒
 *        .addInterceptor(new OkHttpRetryInterceptor(maxRetry)) // 过滤器，设置最大重试次数
 *        .retryOnConnectionFailure(false)                      // 不自动重连
 *        .build();
 * </pre>
 */
public class OkHttpRetryInterceptor implements Interceptor {

    /**
     * 最大重试次数
     */
    private final int maxRetry;

    public OkHttpRetryInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) {
        return retry(chain, maxRetry);
    }

    private Response retry(Chain chain, int retryCet) {
        Request request = chain.request();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (ConnectTimeoutException | ReadTimeoutException e) {
            if (maxRetry > retryCet) {
                return retry(chain, retryCet + 1);
            }
            // interceptor 返回 null 会报 IllegalStateException 异常
            return new Response.Builder().build();
        } catch (Exception e2) {
            // interceptor 返回 null 会报 IllegalStateException 异常
            return new Response.Builder().build();
        }
        return response;
    }
}