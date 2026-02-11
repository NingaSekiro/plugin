package org.aopbuddy.plugin.infra;

import com.intellij.openapi.diagnostic.Logger;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.conn.ConnectTimeoutException;

/**
 * OkHttp重试拦截器<br> 使用：
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

  private static final Logger LOGGER = Logger.getInstance(OkHttpRetryInterceptor.class);

  /**
   * 最大重试次数
   */
  private final int maxRetry;

  public OkHttpRetryInterceptor(int maxRetry) {
    this.maxRetry = maxRetry;
  }

  @Override
  public Response intercept(Chain chain) {
    return retry(chain, 0);
  }

  private Response retry(Chain chain, int retryCet) {
    Request request = chain.request();
    Response response;
    try {
      response = chain.proceed(request);
    } catch (Throwable e) {
      LOGGER.info("Request failed, attempt " + (retryCet + 1) + " of " + (maxRetry + 1));
      
      if (retryCet < maxRetry) {
        try {
          // 短暂延迟后重试，避免立即重试导致的连接风暴
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
        return retry(chain, retryCet + 1);
      }
      
      LOGGER.warn("Max retry attempts reached, returning error response");
      // 无论什么异常，达到最大重试次数后都返回错误响应
      return new Response.Builder()
          .code(503)
          .protocol(Protocol.HTTP_1_1)
          .message("Service Unavailable")
          .request(request)
          .body(ResponseBody.create("connection failed", MediaType.parse("text/plain")))
          .build();
    }
    return response;
  }
}
