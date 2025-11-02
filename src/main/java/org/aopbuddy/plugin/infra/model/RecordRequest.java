package org.aopbuddy.plugin.infra.model;

import lombok.Data;

import java.util.List;

@Data
public class RecordRequest {

    private String projectId;

    private List<String> config;

    private boolean start;
}
