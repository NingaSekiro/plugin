package org.aopbuddy.plugin.infra.util;

import okhttp3.OkHttpClient;
import org.aopbuddy.plugin.infra.OkhttpInterceptor;

import java.util.concurrent.TimeUnit;

public class OkClientUtil {
    public static OkHttpClient getOkHttpClient(int timeout, int maxRetryCnt) {
        return new OkHttpClient().newBuilder()
                .callTimeout(timeout, TimeUnit.MILLISECONDS)
                .addInterceptor(new OkhttpInterceptor(maxRetryCnt)) //过滤器，设置最大重试次数
                .retryOnConnectionFailure(true) //自动重连
                .build();

    }
}
