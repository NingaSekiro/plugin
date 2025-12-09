package org.aopbuddy.plugin.infra.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Graph {
  private List<TreeNode> nodes = new ArrayList<>();

}
