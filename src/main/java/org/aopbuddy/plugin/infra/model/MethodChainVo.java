package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.record.CallRecordDo;
import lombok.Data;
import org.aopbuddy.plugin.infra.util.MermaidConverter;

@Data
public class MethodChainVo {
        private String threadName;
        private String methodChain;
        private Long id;
        private int callChainId;

    public static MethodChainVo toMethodChain(CallRecordDo callRecordDo) {
        MethodChainVo methodChainVo = new MethodChainVo();
        MermaidConverter.SimplifiedMethod simplifiedMethod = MermaidConverter.simplifyMethod(callRecordDo.getMethod());
        methodChainVo.setMethodChain(String.format("%s %s %s()",
                simplifiedMethod.getReturnSimpleClassName(),
                simplifiedMethod.getSimpleTargetClassName(),
                simplifiedMethod.getMethodName()));
        methodChainVo.setId(callRecordDo.getId());
        methodChainVo.setThreadName(callRecordDo.getThreadName());
        methodChainVo.setCallChainId(callRecordDo.getCallChainId());
        return methodChainVo;
    }
}
