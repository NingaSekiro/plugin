package org.aopbuddy.plugin.infra.util;

import com.aopbuddy.bytekit.MethodInfo;
import com.aopbuddy.infrastructure.StringUtils;
import com.aopbuddy.record.CallRecordDo;

import java.util.*;

/**
 * 将方法调用列表转换为mermaid时序图代码
 */
public class MermaidConverter {

  /**
   * 将方法调用列表转换为mermaid时序图代码
   *
   * @param methodCalls 方法调用列表
   * @return mermaid时序图代码
   */
  public static String convertToMermaid(List<CallRecordDo> methodCalls) {
    Set<String> participants = new LinkedHashSet<>();
    participants.add("Thread");
    String methodCallBuilder = dfsTargetPath(0, methodCalls);
    StringBuilder participantsDefinitions = new StringBuilder();
    // 添加所有参与者定义
    for (String participant : participants) {
      participantsDefinitions.append(String.format("    participant %s\n", participant));
    }

    return String.format("sequenceDiagram\n%s%s", participantsDefinitions, methodCallBuilder);
  }

  //目标path流程,只取methodCalls中的调用记录
  private static String dfsTargetPath(Integer index,List<CallRecordDo> methodCalls) {
    // methodCalls排序
    if (index >= methodCalls.size()) {
      return "";
    }
    methodCalls.sort(Comparator.comparingLong(CallRecordDo::getThreadLocalMethodId));
    CallRecordDo callRecordDo = methodCalls.get(index);
    CallRecordDo parentCallRecordDo = new CallRecordDo();
    parentCallRecordDo.setMethod("1|Thread|123");
    for (int i = index - 1; i >= 0; i--) {
      if (callRecordDo.getPath().contains(methodCalls.get(i).getPath())
          && methodCalls.get(i).getPath().length() < callRecordDo.getPath().length()) {
        parentCallRecordDo = methodCalls.get(i);
        break;
      }
    }
    String[] info = StringUtils.splitMethodInfo(callRecordDo.getMethod());
    MethodInfo methodInfo = MethodInfo.builder().methodAccess(info[0]).className(info[1])
        .methodName(info[2]).build();
    String[] split = methodInfo.getClassName().split("\\.");
    String simpleClassName = split[split.length - 1];
    String[] split1 = StringUtils.splitMethodInfo(parentCallRecordDo.getMethod())[1].split("\\.");
    String caller = split1[split1.length - 1];
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(String.format("    %s->>+%s: %s\n",
        caller, simpleClassName, callRecordDo.getId() + "-" + methodInfo.getMethodName() + "()"));
    stringBuilder.append(dfsTargetPath(index + 1, methodCalls));
    if (caller.equals(simpleClassName)) {
      stringBuilder.append(String.format("    deactivate %s\n", caller));
    } else {
      stringBuilder.append(String.format("    %s-->>-%s: \n", simpleClassName, caller));
    }
    return stringBuilder.toString();
  }
}