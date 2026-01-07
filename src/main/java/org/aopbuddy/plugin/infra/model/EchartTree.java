package org.aopbuddy.plugin.infra.model;

import com.aopbuddy.infrastructure.record.CallRecordDo;
import com.aopbuddy.infrastructure.util.StringUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.Data;

@Data
public class EchartTree {

  private String name;
  private String id;
  private List<EchartTree> children;


  public static EchartTree build(List<CallRecordDo> nodes, int rootIndex) {
    if (nodes == null || nodes.isEmpty() || rootIndex >= nodes.size()) {
      return null;
    }

    // 按下标创建同序号的 TreeNode 数组
    EchartTree[] treeArr = new EchartTree[nodes.size()];

    // 根节点先创建
    treeArr[rootIndex] = make(nodes.get(rootIndex));

    // BFS 队列
    Queue<Integer> queue = new LinkedList<>();
    queue.add(rootIndex);

    while (!queue.isEmpty()) {
      int parentIndex = queue.poll();
      CallRecordDo parentRecord = nodes.get(parentIndex);
      EchartTree parentNode = treeArr[parentIndex];

      for (Integer childIndex : parentRecord.getChildIds()) {

        treeArr[childIndex] = make(nodes.get(childIndex));
        queue.add(childIndex);

        parentNode.getChildren().add(treeArr[childIndex]);
      }
    }

    return treeArr[rootIndex];
  }

  private static EchartTree make(CallRecordDo callRecordDo) {
    EchartTree echartTree = new EchartTree();
    echartTree.setId(String.valueOf(callRecordDo.getThreadLocalMethodId()));
    String[] info = StringUtils.splitMethodInfo(callRecordDo.getMethod());
    String[] split = info[0].split("\\.");
    String simpleClassName = split[split.length - 1];
    echartTree.setName(simpleClassName + "." + info[1] + "()");
    echartTree.setChildren(new LinkedList<>());
    return echartTree;
  }
}
