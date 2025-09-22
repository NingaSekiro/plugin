package org.aopbuddy.plugin1.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpServer {
    private String ip;
    private int port;
}
