package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.infrastructure.StringUtils;
import com.aopbuddy.record.CallRecordDo;
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
        String[] strings = StringUtils.splitMethodInfo(callRecordDo.getMethod());
        methodChainVo.setMethodChain(String.format("%s %s()",
                strings[1],
                strings[2]));
        methodChainVo.setId(callRecordDo.getId());
        methodChainVo.setThreadName(callRecordDo.getThreadName());
        methodChainVo.setCallChainId(callRecordDo.getCallChainId());
        methodChainVo.setMessage(callRecordDo.getMessage());
        return methodChainVo;
    }
}
