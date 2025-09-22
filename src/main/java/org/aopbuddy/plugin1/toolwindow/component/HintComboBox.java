package org.aopbuddy.plugin1.toolwindow.component;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;

public class HintComboBox<E> extends ComboBox<E> {
    private String hint;


    public HintComboBox(int i) {
        super(i);
    }


    // 可以提供修改提示文字的方法
    public void setHint(String hint) {
        this.hint = hint;
        init();
        repaint();
    }

    private void init() {
        // 设置默认渲染器
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                // 如果value是null，显示提示文字
                String displayText = (value == null) ? hint : value.toString();

                return super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
            }
        });
        // 初始化时选中null，使提示显示
        setSelectedItem(null);
    }

}
