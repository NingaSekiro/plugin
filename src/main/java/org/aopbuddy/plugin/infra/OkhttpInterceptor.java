package org.aopbuddy.plugin.infra;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OkhttpInterceptor implements Interceptor {
    // 最大重试次数
    private int maxRentry;

    public OkhttpInterceptor(int maxRentry) {
        this.maxRentry = maxRentry;
    }

    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        /* 递归 2次下发请求，如果仍然失败 则返回 null ,但是 intercept must not return null.
         * 返回 null 会报 IllegalStateException 异常
         * */
        return retry(chain, 0);//这个递归真的很舒服
    }

    Response retry(Chain chain, int retryCent) {
        Request request = chain.request();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            if (maxRentry > retryCent) {
                response = retry(chain, retryCent + 1);
            }else {
                throw new IOException(e);
            }
        } finally {
            return response;
        }
    }
}
