package org.aopbuddy.plugin.infra.model;

import lombok.Data;


@Data
public class MermaidRequest {
  private String record;
  private int callChainId;
}
