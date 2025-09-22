package org.aopbuddy.plugin.toolwindow.component

import com.intellij.openapi.ui.ComboBox
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class HintComboBox<E> : ComboBox<E> {
    private var hint: String? = null

    constructor(i: Int) : super(i)

    // 可以提供修改提示文字的方法
    fun setHint(hint: String?) {
        this.hint = hint
        init()
        repaint()
    }

    private fun init() {
        // 设置默认渲染器
        renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                // 如果value是null，显示提示文字
                val displayText = if (value == null) hint else value.toString()
                return super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus)
            }
        }
        // 初始化时选中null，使提示显示
        selectedItem = null
    }
}