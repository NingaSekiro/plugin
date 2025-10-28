package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.record.CallRecordDo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodVo extends CallRecordDo {
    private boolean inboundCall;

    public static MethodVo toMethod(CallRecordDo callRecordDo) {
        MethodVo methodVo = new MethodVo();
        methodVo.setId(callRecordDo.getId());
        methodVo.setCallChainId(callRecordDo.getCallChainId());
        methodVo.setMethod(callRecordDo.getMethod());
        methodVo.setInboundCall(callRecordDo.getArgs() != null);
        return methodVo;
    }
}
