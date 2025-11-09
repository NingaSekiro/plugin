package org.aopbuddy.plugin.infra.util;

import com.aopbuddy.record.CallRecordDo;
import com.aopbuddy.record.SimplifiedMethod;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.aopbuddy.record.MethodChainKey.simplifyMethod;
import static org.junit.Assert.assertNotNull;

class MermaidConverterTest {

    @Test
    void convertToMermaid() {
        // 准备测试数据
        List<CallRecordDo> methodCalls = Arrays.asList(
                new CallRecordDo(1L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", "DDD", null, null, null, null),
                new CallRecordDo(2L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(3L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(4L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(5L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(6L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", null, null, null, null, null)
        );

        // 执行转换
        String result = MermaidConverter.convertToMermaid(methodCalls);

        // 验证结果
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void convertToMermaid_own() {
        // 准备测试数据
        List<CallRecordDo> methodCalls = Arrays.asList(
                new CallRecordDo(1L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", "DDD", null, null, null, null),
                new CallRecordDo(2L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(3L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(4L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(5L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(6L, 1, "public void com.example.demo.controller.DemoController.ddd(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(7L, 1, "public void com.example.demo.controller.DemoController.ddd(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(8L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", null, null, null, null, null)
        );

        // 执行转换
        String result = MermaidConverter.convertToMermaid(methodCalls);

        // 验证结果
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void convertToMermaid_own_2() {
        // 准备测试数据
        List<CallRecordDo> methodCalls = Arrays.asList(
                new CallRecordDo(1L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", "DDD", null, null, null, null),
                new CallRecordDo(2L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(3L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(4L, 1, "public void com.example.demo.controller.DemoController.middlec(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(5L, 1, "public void com.example.demo.controller.DemoController.ddd(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(6L, 1, "public void com.example.demo.controller.DemoController.ccc(java.lang.Integer)", "DDD", null, null, null, null),
                new CallRecordDo(7L, 1, "public void com.example.demo.controller.DemoController.ccc(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(8L, 1, "public void com.example.demo.controller.DemoController.ddd(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(9L, 1, "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)", null, null, null, null, null),
                new CallRecordDo(10L, 1, "public void com.example.demo.controller.DemoController.testScheduled5()", null, null, null, null, null)
        );

        // 执行转换
        String result = MermaidConverter.convertToMermaid(methodCalls);

        // 验证结果
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    void convertToMermaid_own_3() {

        List<CallRecordDo> methodCalls = Arrays.asList(
                new CallRecordDo(1L, 1, "public void com.fubukiss.rikky.filter.LoginCheckFilter.doFilter()", "DDD", null, null, null, null),
                new CallRecordDo(2L, 1, "public boolean com.fubukiss.rikky.filter.LoginCheckFilter.check(java.lang.String[],java.lang.String)", "DDD", null, null, null, null),
                new CallRecordDo(3L, 1, "public boolean com.fubukiss.rikky.filter.LoginCheckFilter.check(java.lang.String[],java.lang.String)", "null", null, null, null, null),
                new CallRecordDo(4L, 1, "public void com.fubukiss.rikky.filter.LoginCheckFilter.doFilter()", "null", null, null, null, null)
                );
        String result = MermaidConverter.convertToMermaid(methodCalls);

        // 验证结果
        assertNotNull(result);
        System.out.println(result);
    }


    @Test
    void test_simplifiedMethod() {
        // 准备测试数据
        String methodDescription = "public com.example.demo.controller.Model com.example.demo.controller.DemoController1.middle(java.lang.Integer)";

        // 执行转换
        SimplifiedMethod simplifiedMethod = simplifyMethod(methodDescription);

        // 验证结果
        assertNotNull(simplifiedMethod);
        System.out.println(simplifiedMethod);
    }
}