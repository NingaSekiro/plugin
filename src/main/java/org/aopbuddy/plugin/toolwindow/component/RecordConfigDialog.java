package org.aopbuddy.plugin.toolwindow.component;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 录制配置对话框：合并类名和方法名输入
 */
public class RecordConfigDialog extends DialogWrapper {
    private final JBTextField classNameField = new JBTextField();
    private final JBTextField methodNameField = new JBTextField();

    public RecordConfigDialog() {
        super(true); // 使用模态对话框
        setTitle("录制配置");
        init(); // 初始化对话框
    }

    /**
     * 创建对话框中心面板（包含类名和方法名输入框）
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // 使用FormBuilder快速构建表单布局
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("监控类名（支持通配符*）:"), classNameField, 1, false)
                .addLabeledComponent(new JBLabel("监控方法名（支持通配符*）:"), methodNameField, 1, false)
                .addComponentFillVertically(new JPanel(), 0) // 垂直填充空白
                .getPanel();
    }

    /**
     * 输入校验：确保类名和方法名不为空
     */
    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String className = getClassName().trim();
        String methodName = getMethodName().trim();

        if (className.isEmpty()) {
            return new ValidationInfo("类名不能为空", classNameField);
        }
        if (methodName.isEmpty()) {
            return new ValidationInfo("方法名不能为空", methodNameField);
        }
        return null; // 校验通过
    }

    // Getter方法：供外部获取输入值
    public String getClassName() {
        return classNameField.getText();
    }

    public String getMethodName() {
        return methodNameField.getText();
    }
}