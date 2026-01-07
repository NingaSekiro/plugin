package groovy

import com.aopbuddy.infrastructure.retransform.pointcut.MethodPointcut


MethodPointcut pointcut = MethodPointcut.of(
        params.className, params.methodName, "(..)");
Context.unregisterAdvisor(pointcut, TraceListener.class);
CaffeineCache.getCache().invalidateAll();
