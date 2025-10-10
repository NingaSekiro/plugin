package groovy

import com.aopbuddy.agent.TraceListener
import com.aopbuddy.aspect.MethodPointcut
import com.aopbuddy.retransform.Context
import com.aopbuddy.retransform.Listener

MethodPointcut pointcut = MethodPointcut.of(
        params.className, params.methodName, "(..)");
Listener listener = new TraceListener();
Context.registerAdvisor(pointcut, listener);

