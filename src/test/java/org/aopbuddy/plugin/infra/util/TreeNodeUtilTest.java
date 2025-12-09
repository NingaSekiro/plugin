package org.aopbuddy.plugin.infra.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.aopbuddy.record.CallRecordDo;
import java.util.ArrayList;
import java.util.List;
import org.aopbuddy.plugin.infra.model.TreeNode;
import org.junit.jupiter.api.Test;

public class TreeNodeUtilTest {

  @Test
  public void buildTree() {
    CallRecordDo callRecordDo = new CallRecordDo();
    callRecordDo.setMethod("1|com.aopbuddytest.TargetService|greetString");
    callRecordDo.setThreadLocalMethodId(0);
    callRecordDo.setChildIds(List.of(1));

    CallRecordDo callRecordDo1 = new CallRecordDo();
    callRecordDo1.setMethod("1|com.aopbuddytest.TargetService|greet");
    callRecordDo1.setThreadLocalMethodId(1);
    callRecordDo1.setChildIds(List.of());
    // 准备测试数据
    List<CallRecordDo> methodCalls = new ArrayList<>();
    methodCalls.add(callRecordDo);
    methodCalls.add(callRecordDo1);

    TreeNode treeNode = TreeNodeUtil.buildTree(methodCalls, 0);
    assertNotNull(treeNode);
    System.out.println("dddd");
  }
}