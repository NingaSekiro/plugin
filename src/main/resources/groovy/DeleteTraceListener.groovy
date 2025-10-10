package groovy

import com.aopbuddy.agent.TraceListener
import com.aopbuddy.aspect.MethodPointcut
import com.aopbuddy.record.CaffeineCache
import com.aopbuddy.retransform.Context

MethodPointcut pointcut = MethodPointcut.of(
        params.className, params.methodName, "(..)");
Context.unregisterAdvisor(pointcut, TraceListener.class);
CaffeineCache.getCache().invalidateAll();
