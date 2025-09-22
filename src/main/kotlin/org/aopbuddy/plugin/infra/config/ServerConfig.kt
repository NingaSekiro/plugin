package org.aopbuddy.plugin.infra.config

import com.intellij.openapi.components.*
import org.aopbuddy.plugin.infra.model.HttpServer

@State(
    name = "ServerConfig",
    storages = [Storage("aopBuddyServerConfig.xml")]
)
@Service
class ServerConfig : PersistentStateComponent<ServerConfig> {

    var serverMap: MutableMap<String, HttpServer> = HashMap()

    override fun getState(): ServerConfig {
        return this
    }

    override fun loadState(state: ServerConfig) {
        this.serverMap.clear()
        this.serverMap.putAll(state.serverMap)
    }

    companion object {
        fun getInstance(): ServerConfig {
            return ServiceManager.getService(ServerConfig::class.java)
        }
    }
}