package org.aopbuddy.plugin.infra.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@State(
        name = "ServerConfig",
        storages = @Storage("aopBuddyServerConfig.xml")
)
@Data
public class ServerConfig implements PersistentStateComponent<ServerConfig> {

    private Map<String, HttpServer> serverMap = new HashMap<>();

    public static ServerConfig getInstance() {
        return ServiceManager.getService(ServerConfig.class);
    }

    @Override
    public ServerConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ServerConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
