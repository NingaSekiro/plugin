package groovy

import com.aopbuddy.domain.retransform.listener.Listener
import com.aopbuddy.infrastructure.retransform.listener.TraceListener
import com.aopbuddy.infrastructure.retransform.pointcut.MethodPointcut


MethodPointcut pointcut = MethodPointcut.of(
        params.className, params.methodName, "(..)");
Listener listener = new TraceListener();
Context.registerAdvisor(pointcut, listener);

