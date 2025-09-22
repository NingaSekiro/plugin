package org.aopbuddy.plugin.toolwindow.panel

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.border.CustomLineBorder
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aopbuddy.plugin.infra.ToolWindowUpdateNotifier
import org.aopbuddy.plugin.infra.config.ServerConfig
import org.aopbuddy.plugin.service.ConsoleStateService
import org.aopbuddy.plugin.service.JvmService
import org.aopbuddy.plugin.toolwindow.component.HintComboBox
import org.aopbuddy.plugin.toolwindow.component.MyEditorTextField
import org.jetbrains.plugins.groovy.GroovyFileType
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class GroovyConsolePanel(project: Project) : OnePixelSplitter(false, "JZ.ConsoleRun", 0.6f) {
    private val coroutineScope = CoroutineScope(Dispatchers.EDT)
    private val consoleStateService = project.getService(ConsoleStateService::class.java)
    private val jvmService = JvmService.getInstance(project)
    private val groovyEditor = MyEditorTextField(project, GroovyFileType.GROOVY_FILE_TYPE).apply {
        border = JBUI.Borders.empty(5)
    }
    private val pidComboBox = HintComboBox<String>(200).apply {
        addItem("1.点此添加JVM进程")
        setSelectedIndex(0)
        addActionListener {
            if (selectedIndex == 0) {
                showJvmProcessSelection()
            } else {
                // 更新状态服务中的选中PID
                consoleStateService.serverName = selectedItem.toString()
                updateClassloaderComboBox()
            }
        }
    }
    private val classloaderComboBox = HintComboBox<String>(200).apply {
        setHint("2.选择classLoader")
        addActionListener {
            // 更新状态服务中的选中ClassLoader
            selectedItem?.let {
                consoleStateService.selectedClassloader = it.toString()
            }
        }
    }
    private val runStatusEditor = MyEditorTextField(project, FileTypes.PLAIN_TEXT).apply {
        border = JBUI.Borders.empty(5)
    }
    private val listenerStatusEditor = MyEditorTextField(project, FileTypes.PLAIN_TEXT).apply {
        border = JBUI.Borders.empty(5)
    }

    init {
        firstComponent = getGroovyConsolePanel()
        secondComponent = getJvmResultInfoPanel()
        project.messageBus.connect().subscribe(
            ToolWindowUpdateNotifier.UPDATE_TOPIC,
            ToolWindowUpdateNotifier { text -> groovyEditor.text = text }
        )
    }


    private fun getGroovyConsolePanel(): JComponent {
        val toolbarPanel = JPanel().apply {
            preferredSize = Dimension(-1, 30)
            layout = BoxLayout(this, 0)
            border = CustomLineBorder(JBUI.insetsBottom(1))
            add(pidComboBox)
            add(classloaderComboBox)
            add(createRunButton())
            add(Box.createHorizontalGlue())
            add(Box.createHorizontalStrut(5))
        }

        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty()
            add(toolbarPanel, BorderLayout.NORTH)
            add(groovyEditor, BorderLayout.CENTER)
        }
    }

    private fun getJvmResultInfoPanel(): JComponent {
        val toolbarPanel = JPanel().apply {
            // 右侧顶部工具栏
            preferredSize = Dimension(-1, 30)
            layout = BoxLayout(this, 0)
            border = CustomLineBorder(JBUI.insetsBottom(1))
            add(createClearActionButton())
            add(Box.createHorizontalGlue())
            add(Box.createHorizontalStrut(5))
        }

        val groovyTabbedPane = JBTabbedPane().apply {
            border = CustomLineBorder(JBUI.insetsTop(1))
            addTab("执行方法结果", runStatusEditor)
            addTab("监控方法结果", listenerStatusEditor)
            addChangeListener {
                if (selectedIndex == 1) {
                    coroutineScope.launch {
                        val eval = jvmService.evalAsync("readListenerLog()")
                        listenerStatusEditor.text = eval
                    }
                }
            }
        }

        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty()
            add(toolbarPanel, BorderLayout.NORTH)
            add(groovyTabbedPane, BorderLayout.CENTER)
        }
    }

    private fun showJvmProcessSelection() {
        coroutineScope.launch {
            try {
                val processList = jvmService.getJvmsAsync()
                // 显示选择对话框
                val selectedProcess = JvmProcessSelectorDialog.showAndGet(processList)

                // 如果用户选择了进程
                if (selectedProcess != null) {
                    if (!ServerConfig.getInstance().serverMap.containsKey(selectedProcess)) {
                        try {
                            jvmService.attachAsync(selectedProcess)
                            updatePidComboBox(selectedProcess)
                        } catch (e: Exception) {
                            Messages.showErrorDialog(
                                "连接JVM进程失败: ${e.message}",
                                "连接错误"
                            )
                        }
                    } else {
                        updatePidComboBox(selectedProcess)
                    }
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(
                    "获取JVM进程列表失败: ${e.message}",
                    "获取错误"
                )
            }
        }
    }

    private fun updatePidComboBox(selectedProcess: String) {
        // 检查PID是否已存在于ComboBox中
        var exists = false
        for (i in 0 until pidComboBox.itemCount) {
            val item = pidComboBox.getItemAt(i)
            if (item != null && item == selectedProcess) {
                exists = true
                break
            }
        }
        // 如果不存在，则添加到ComboBox中
        if (!exists) {
            pidComboBox.addItem(selectedProcess)
            pidComboBox.selectedItem = selectedProcess
            // 更新状态服务中的选中PID
            consoleStateService.serverName = selectedProcess
        }
    }

    private fun updateClassloaderComboBox() {
        coroutineScope.launch {
            try {
                val classloaders = jvmService.getClassloadersAsync()
                classloaderComboBox.removeAllItems()
                for (classloader in classloaders) {
                    classloaderComboBox.addItem(classloader)
                }
                if (classloaders.isNotEmpty()) {
                    classloaderComboBox.selectedIndex = 0
                    // 更新状态服务中的可用ClassLoader列表和选中ClassLoader
                    consoleStateService.availableClassloaders = classloaders
                    consoleStateService.selectedClassloader = classloaders[0]
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(
                    "获取类加载器列表失败: ${e.message}",
                    "获取错误"
                )
            }
        }
    }

    private fun createRunButton(): JButton {
        return JButton().apply {
            icon = AllIcons.Actions.RunAll
            isContentAreaFilled = false
            toolTipText = "执行"
            preferredSize = Dimension(30, 30)
            addActionListener {
                coroutineScope.launch {
                    try {
                        val result = jvmService.evalAsync(groovyEditor.text)
                        runStatusEditor.text = result
                    } catch (e: Exception) {
                        Messages.showErrorDialog(
                            "执行脚本失败: ${e.message}",
                            "执行错误"
                        )
                    }
                }
            }
        }
    }

    private fun createClearActionButton(): JButton {
        return JButton().apply {
            icon = AllIcons.Actions.GC
            isContentAreaFilled = false
            toolTipText = "清除执行结果"
            preferredSize = Dimension(30, -1)
            addActionListener {
                runStatusEditor.text = ""
                listenerStatusEditor.text = ""
            }
        }
    }
}