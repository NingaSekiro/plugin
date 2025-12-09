package org.aopbuddy.plugin.infra.util;

import static org.junit.Assert.assertNotNull;

import com.aopbuddy.record.CallRecordDo;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MermaidConverterTest {

    @Test
    void convertToMermaid_own_2() {
        CallRecordDo callRecordDo = new CallRecordDo();
        callRecordDo.setMethod("1|com.aopbuddytest.TargetService|greetString");
        callRecordDo.setThreadLocalMethodId(0);
        callRecordDo.setPath("0");
        callRecordDo.setChildIds(List.of(1));

        CallRecordDo callRecordDo1 = new CallRecordDo();
        callRecordDo1.setMethod("1|com.aopbuddytest.TargetService|greet");
        callRecordDo1.setThreadLocalMethodId(1);
        callRecordDo1.setPath("0|1");
        callRecordDo1.setChildIds(List.of());
        // 准备测试数据
        List<CallRecordDo> methodCalls = new ArrayList<>();
        methodCalls.add(callRecordDo);
        methodCalls.add(callRecordDo1);

        // 执行转换
        String result = MermaidConverter.convertToMermaid(methodCalls);

        // 验证结果
        assertNotNull(result);
        System.out.println(result);
    }
}