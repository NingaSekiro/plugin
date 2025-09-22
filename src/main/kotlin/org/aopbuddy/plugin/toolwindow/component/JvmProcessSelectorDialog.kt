import cn.hutool.core.lang.Validator
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.coroutineScope
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JComboBox

/**
 * 进程选择对话框组件
 * 使用 JBTabbedPane 分成两个面板：
 * - 本地进程：从提供的进程列表中选择
 * - 远程 JVM：输入 IP 地址（格式：IP:端口）
 * 确定后返回选择的进程字符串（本地）或 IP 字符串（远程），否则 null
 * 只能选择一个面板的值
 */
class JvmProcessSelectorDialog(
    private val processList: List<String> // 本地 JVM 进程列表
) : DialogWrapper(true) {

    private val tabbedPane = JBTabbedPane()
    private val localPanel = JPanel(BorderLayout())
    private val remotePanel = JPanel(BorderLayout())
    private val localComboBox = JComboBox(processList.toTypedArray()) // 使用标准 JComboBox
    private val remoteTextField = JBTextField()
    private var selectedValue: String? = null

    init {
        title = "选择 JVM 连接方式"
        initValidation()
        init()
        setupUI()
    }

    private fun setupUI() {
        // 本地进程面板
        val localLabel = JBLabel("请选择本地 JVM 进程:")
        localLabel.border = JBUI.Borders.empty(10)
        localPanel.add(localLabel, BorderLayout.NORTH)

        // 使用 JComboBox 显示进程列表
        localComboBox.preferredSize = Dimension(300, 30)
        localPanel.add(localComboBox, BorderLayout.CENTER)

        // 远程 JVM 面板
        val remoteLabel =
            JBLabel("请输入远程JVM IP和agent http port端口 (e.g., 192.168.1.1:8888),java agent需预先安装，默认端口8888:")
        remoteLabel.border = JBUI.Borders.empty(10)
        remotePanel.add(remoteLabel, BorderLayout.NORTH)

        remoteTextField.preferredSize = Dimension(300, 30)
        remotePanel.add(remoteTextField, BorderLayout.CENTER)

        // 添加到 TabbedPane
        tabbedPane.addTab("本地进程", localPanel)
        tabbedPane.addTab("远程 JVM", remotePanel)
    }

    override fun createCenterPanel(): JComponent {
        return tabbedPane
    }

    override fun doOKAction() {
        // 根据当前 tab 验证并设置返回值
        when (tabbedPane.selectedIndex) {
            0 -> { // 本地进程
                selectedValue = localComboBox.selectedItem as String?
            }

            1 -> { // 远程 JVM
                selectedValue = remoteTextField.text
            }
        }
        super.doOKAction()
    }

    // 自定义验证（可选，增强 UI 反馈）
    override fun doValidate(): ValidationInfo? {
        return when (tabbedPane.selectedIndex) {
            0 -> if (localComboBox.selectedItem == null) ValidationInfo("请选择本地进程", localComboBox) else null
            1 -> {
                val serverName = remoteTextField.text
                if (serverName.isNullOrBlank()) ValidationInfo("请输入 IP:端口", remoteTextField)
                else if (serverName.split(":").size != 2 || !(Validator.isIpv4(serverName.split(":")[0]) && serverName.split(
                        ":"
                    )[1].matches(Regex("\\d+")))
                ) ValidationInfo(
                    "IP:端口 格式无效",
                    remoteTextField
                )
                else null
            }

            else -> ValidationInfo("请选择一个面板")
        }
    }

    // 获取返回值（在 doOKAction 后调用）
    fun getSelectedValue(): String? = selectedValue

    companion object {
        // 静态工厂方法：异步获取进程列表并显示对话框
        suspend fun showAndGet(processList: List<String>): String? = coroutineScope {
            val dialog = JvmProcessSelectorDialog(processList)
            if (dialog.showAndGet()) {
                dialog.getSelectedValue()
            } else {
                null
            }
        }
    }
}