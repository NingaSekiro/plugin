package org.aopbuddy.plugin.infra.model;

import lombok.Data;

import java.util.Set;

@Data
public class ConfigResp {
    private Set<String> packageNames;
    private boolean status;
}
