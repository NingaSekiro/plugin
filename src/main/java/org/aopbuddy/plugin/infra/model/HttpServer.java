package org.aopbuddy.plugin.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpServer {
    private String name;
    private String ip;
    private int port;

}
