package org.aopbuddy.plugin.infra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TreeNode {
  private String id;

  @JsonProperty("data")
  private TreeData treeData;
  
  private List<TreeNode> children = new ArrayList<>();
}
