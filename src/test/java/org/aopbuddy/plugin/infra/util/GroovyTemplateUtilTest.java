package org.aopbuddy.plugin.infra.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GroovyTemplateUtilTest {
    @Test
    public void generateCode() {
        Map<String, String> params = new HashMap<>();
        params.put("className", "com.aopbuddy.mapper.UserMapper");
        params.put("methodName", "insert");
        String code = GroovyTemplateUtil.generateCode("RegisterTraceListener", params);
        System.out.println(code);
    }


}