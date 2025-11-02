package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.record.CallRecordDo;
import lombok.Data;

@Data
public class MethodChainVo {
    private String threadName;
    private String methodChain;

    public static MethodChainVo toMethodChain(CallRecordDo callRecordDo) {
        MethodChainVo methodChainVo = new MethodChainVo();
        methodChainVo.setMethodChain(callRecordDo.getMethod());
        return methodChainVo;
    }
}
