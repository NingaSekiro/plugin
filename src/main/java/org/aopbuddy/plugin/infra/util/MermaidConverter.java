package org.aopbuddy.plugin.infra.util;


import com.aopbuddy.infrastructure.record.CallRecordDo;
import com.aopbuddy.infrastructure.util.StringUtils;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
  private static String dfsTargetPath(Integer index, List<CallRecordDo> methodCalls) {
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
    String[] split = info[0].split("\\.");
    String simpleClassName = split[split.length - 1];
    String[] split1 = StringUtils.splitMethodInfo(parentCallRecordDo.getMethod())[1].split("\\.");
    String caller = split1[split1.length - 1];
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(String.format("    %s->>+%s: %s\n",
        caller, simpleClassName, callRecordDo.getId() + "-" + info[1] + "()"));
    stringBuilder.append(dfsTargetPath(index + 1, methodCalls));
    if (caller.equals(simpleClassName)) {
      stringBuilder.append(String.format("    deactivate %s\n", caller));
    } else {
      stringBuilder.append(String.format("    %s-->>-%s: \n", simpleClassName, caller));
    }
    return stringBuilder.toString();
  }
}