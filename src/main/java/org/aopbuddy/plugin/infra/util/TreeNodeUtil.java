package org.aopbuddy.plugin.infra.util;

import com.aopbuddy.infrastructure.StringUtils;
import com.aopbuddy.record.CallRecordDo;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.aopbuddy.plugin.infra.model.TreeData;
import org.aopbuddy.plugin.infra.model.TreeNode;

public class TreeNodeUtil {

  public static TreeNode buildTree(List<CallRecordDo> nodes, int rootIndex) {
    if (nodes == null || nodes.isEmpty() || rootIndex >= nodes.size()) {
      return null;
    }

    // 按下标创建同序号的 TreeNode 数组
    TreeNode[] treeArr = new TreeNode[nodes.size()];

    // 根节点先创建
    treeArr[rootIndex] = make(nodes.get(rootIndex));

    // BFS 队列
    Queue<Integer> queue = new LinkedList<>();
    queue.add(rootIndex);

    while (!queue.isEmpty()) {
      int parentIndex = queue.poll();
      CallRecordDo parentRecord = nodes.get(parentIndex);
      TreeNode parentNode = treeArr[parentIndex];

      for (Integer childIndex : parentRecord.getChildIds()) {

        treeArr[childIndex] = make(nodes.get(childIndex));
        queue.add(childIndex);

        parentNode.getChildren().add(treeArr[childIndex]);
      }
    }

    return treeArr[rootIndex];
  }


  public static TreeNode make(CallRecordDo callRecordDo) {
    TreeNode treeNode = new TreeNode();
    treeNode.setId(String.valueOf(callRecordDo.getThreadLocalMethodId()));
    String[] info = StringUtils.splitMethodInfo(callRecordDo.getMethod());
    String[] split = info[1].split("\\.");
    String simpleClassName = split[split.length - 1];
    TreeData treeData = new TreeData();
    treeData.setId(String.valueOf(callRecordDo.getId()));
    treeData.setName(simpleClassName + "." + info[2] + "()");
    treeNode.setTreeData(treeData);
    return treeNode;
  }
}
