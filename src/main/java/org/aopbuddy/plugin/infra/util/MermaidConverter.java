package org.aopbuddy.plugin.infra.util;

import com.aopbuddy.record.CallRecordDo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将方法调用列表转换为mermaid时序图代码
 */
public class MermaidConverter {
    
    /**
     * 将方法调用列表转换为mermaid时序图代码
     * @param methodCalls 方法调用列表
     * @return mermaid时序图代码
     */
    public static String convertToMermaid(List<CallRecordDo> methodCalls) {
        Set<String> participants = new LinkedHashSet<>();
        participants.add("Thread");
        
        // 使用栈来记录调用链
        Stack<String> callStack = new Stack<>();
        callStack.push("Thread");
        
        StringBuilder methodCallBuilder = new StringBuilder();
        for (CallRecordDo call : methodCalls) {
            SimplifiedMethod simplifiedMethod = simplifyMethod(call.getMethod());
            String caller = callStack.peek();
            participants.add(simplifiedMethod.getSimpleTargetClassName());
            
            if (CallRecordDo.isInboundCall(call)) {
                if (caller.equals(simplifiedMethod.getSimpleTargetClassName())) {
                    // 同一类的调用，不激活和去激活
                    methodCallBuilder.append(String.format("    %s->>%s: %s\n", 
                        caller, simplifiedMethod.getSimpleTargetClassName(), call.getId() + "." + simplifiedMethod.getMethodName()));
                    callStack.push(simplifiedMethod.getSimpleTargetClassName());
                } else {
                    // 不同类的调用，激活目标类
                    methodCallBuilder.append(String.format("    %s->>+%s: %s\n", 
                        caller, simplifiedMethod.getSimpleTargetClassName(), call.getId() + "." + simplifiedMethod.getMethodName()));
                    callStack.push(simplifiedMethod.getSimpleTargetClassName());
                }
            } else {
                // 同一类的调用
                if (callStack.size() > 1 && 
                    callStack.get(callStack.size() - 2).equals(simplifiedMethod.getSimpleTargetClassName())) {
                    callStack.pop();
                } else {
                    callStack.pop();
                    String returnTo = callStack.peek();
                    methodCallBuilder.append(String.format("    %s-->>-%s: %s\n", caller, returnTo, call.getId() + "."));
                }
            }
        }
        
        StringBuilder participantsDefinitions = new StringBuilder();
        // 添加所有参与者定义
        for (String participant : participants) {
            participantsDefinitions.append(String.format("    participant %s\n", participant));
        }
        
        return String.format("sequenceDiagram\n%s%s", participantsDefinitions.toString(), methodCallBuilder.toString());
    }
    
    /**
     * 简化方法签名
     * @param fullMethod 方法签名字符串
     * @return 简化后的方法信息
     */
    public static SimplifiedMethod simplifyMethod(String fullMethod) {
        String[] parts = fullMethod.split(" ");
        String permission = parts[0];
        String returnSimpleClassName = parts[1].substring(parts[1].lastIndexOf('.') + 1);
        
        String[] methodParts = parts[2].split("\\(");
        String[] classNameParts = methodParts[0].split("\\.");
        String simpleTargetClassName = classNameParts[classNameParts.length - 2];
        String methodName = classNameParts[classNameParts.length - 1] + "()";
        
        Pattern pattern = Pattern.compile("(\\w+\\([^)]*\\))$");
        Matcher matcher = pattern.matcher(fullMethod);
        String methodDescription = matcher.find() ? matcher.group(1) : "";
        return new SimplifiedMethod(permission, returnSimpleClassName, simpleTargetClassName, methodName);
    }
    
    /**
     * 方法调用数据类
     */
    public static class MethodCall {
        private String method;
        private boolean inboundCall;
        
        public MethodCall(String method, boolean inboundCall) {
            this.method = method;
            this.inboundCall = inboundCall;
        }
        
        public String getMethod() {
            return method;
        }
        
        public boolean isInboundCall() {
            return inboundCall;
        }
    }
    
    /**
     * 简化后的方法信息数据类
     */
    public static class SimplifiedMethod {
        private final String permission;
        private final String returnSimpleClassName;
        private final String simpleTargetClassName;
        private final String methodName;
        
        public SimplifiedMethod(String permission, String returnSimpleClassName, 
                               String simpleTargetClassName, String methodName) {
            this.permission = permission;
            this.returnSimpleClassName = returnSimpleClassName;
            this.simpleTargetClassName = simpleTargetClassName;
            this.methodName = methodName;
        }
        
        public String getPermission() {
            return permission;
        }
        
        public String getReturnSimpleClassName() {
            return returnSimpleClassName;
        }
        
        public String getSimpleTargetClassName() {
            return simpleTargetClassName;
        }
        
        public String getMethodName() {
            return methodName;
        }
    }
}
