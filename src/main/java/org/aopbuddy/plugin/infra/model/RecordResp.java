package org.aopbuddy.plugin.infra.model;

import lombok.Data;

import java.util.List;

@Data
public class RecordResp {
    private int code;
    private String record;
    private List<MethodChainVo> methodChains;
}
