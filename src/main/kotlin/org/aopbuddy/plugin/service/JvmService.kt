package org.aopbuddy.plugin.service

import cn.hutool.core.lang.Validator
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.sun.tools.attach.AgentInitializationException
import com.sun.tools.attach.AgentLoadException
import com.sun.tools.attach.AttachNotSupportedException
import com.sun.tools.attach.VirtualMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.aopbuddy.plugin.infra.config.ServerConfig
import org.aopbuddy.plugin.infra.model.EvalRequest
import org.aopbuddy.plugin.infra.model.HttpServer
import org.aopbuddy.plugin.infra.util.PluginPathUtil
import org.aopbuddy.plugin.infra.util.PortUtil
import java.io.File

@Service(Service.Level.PROJECT)
class JvmService(private val project: Project) {
    private val consoleStateService = project.getService(ConsoleStateService::class.java)

    companion object {
        @JvmStatic
        fun getInstance(project: Project): JvmService {
            return project.getService(JvmService::class.java)
        }
    }

    suspend fun getJvmsAsync(): List<String> = withContext(Dispatchers.IO) {
        val vms = VirtualMachine.list()
        val processList = mutableListOf<String>()
        // 构造进程信息列表
        for (vm in vms) {
            val displayName = vm.displayName()
            val id = vm.id()
            // 格式化显示信息：PID - 进程名
            val displayText = "$id - ${if (displayName.isEmpty()) "Unknown" else displayName}"
            processList.add(displayText)
        }
        processList
    }

    suspend fun attachAsync(serverName: String) = withContext(Dispatchers.IO) {
        if (!Validator.isIpv4(serverName.split(":")[0])) {
            try {
                val pid = serverName.split(" - ")[0]
                val vm = VirtualMachine.attach(pid)
                val pluginDir = PluginPathUtil.getPluginDirectory()
                val freePort = PortUtil.findFreePort()
                try {
                    if (pluginDir == null) {
                        throw RuntimeException("无法获取插件目录")
                    }
                    vm.loadAgent(
                        pluginDir.absolutePath + File.separator + "lib" + File.separator + "agent-jar-with-dependencies.jar",
                        freePort.toString()
                    )
                } catch (e: AgentLoadException) {
                    if ("0" != e.message) {
                        throw e
                    }
                }
                val instance = ServerConfig.getInstance()
                instance.serverMap[serverName] = HttpServer("127.0.0.1", freePort)
                vm.detach()
            } catch (e: AttachNotSupportedException) {
                throw RuntimeException(e)
            } catch (e: java.io.IOException) {
                throw RuntimeException(e)
            } catch (e: AgentInitializationException) {
                throw RuntimeException(e)
            } catch (e: AgentLoadException) {
                throw RuntimeException(e)
            }
        } else {
            val instance = ServerConfig.getInstance()
            instance.serverMap[serverName] = HttpServer(serverName.split(":")[0], serverName.split(":")[1].toInt())
        }
    }

    suspend fun getClassloadersAsync(): List<String> = withContext(Dispatchers.IO) {
        val server = ServerConfig.getInstance().serverMap[consoleStateService.serverName]
        val result = HttpUtil.get("http://" + server!!.ip + ":" + server.port + "/classloaders")
        JSONUtil.toList(result, String::class.java)
    }

    suspend fun evalAsync(script: String): String = withContext(Dispatchers.IO) {
        val serverName = consoleStateService.serverName
        val evalRequest = EvalRequest(
            serverName,
            consoleStateService.selectedClassloader,
            script
        )
        val server = ServerConfig.getInstance().serverMap[serverName]
        HttpUtil.post(
            "http://" + server!!.ip + ":" + server.port + "/eval",
            JSONUtil.toJsonStr(evalRequest)
        )
    }
}