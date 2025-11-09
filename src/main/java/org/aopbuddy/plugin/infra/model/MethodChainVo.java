package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.record.CallRecordDo;
import com.aopbuddy.record.MethodChainKey;
import com.aopbuddy.record.SimplifiedMethod;
import lombok.Data;

@Data
public class MethodChainVo {
        private String threadName;
        private String message;
        private String methodChain;
        private Long id;
        private int callChainId;

    public static MethodChainVo toMethodChain(CallRecordDo callRecordDo) {
        MethodChainVo methodChainVo = new MethodChainVo();
        SimplifiedMethod simplifiedMethod = MethodChainKey.simplifyMethod(callRecordDo.getMethod());
        methodChainVo.setMethodChain(String.format("%s %s %s()",
                simplifiedMethod.getReturnSimpleClassName(),
                simplifiedMethod.getSimpleTargetClassName(),
                simplifiedMethod.getMethodName()));
        methodChainVo.setId(callRecordDo.getId());
        methodChainVo.setThreadName(callRecordDo.getThreadName());
        methodChainVo.setCallChainId(callRecordDo.getCallChainId());
        methodChainVo.setMessage(callRecordDo.getMessage());
        return methodChainVo;
    }
}
