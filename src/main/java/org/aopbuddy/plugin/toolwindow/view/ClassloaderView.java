package org.aopbuddy.plugin.toolwindow.view;

import lombok.Getter;
import org.aopbuddy.plugin.toolwindow.component.HintComboBox;
import org.aopbuddy.plugin.toolwindow.model.ClassloaderModel;

public class ClassloaderView {
    private final ClassloaderModel classloaderModel;

    @Getter
    private final HintComboBox<String> classloaderComboBox;

    public ClassloaderView(ClassloaderModel classloaderModel) {
        this.classloaderModel = classloaderModel;
        this.classloaderComboBox = new HintComboBox<>(200);
        this.classloaderComboBox.setHint("2.选择classLoader");
        this.classloaderComboBox.setModel(classloaderModel);
        this.classloaderComboBox.addActionListener(e -> {
            // 更新状态服务中的选中ClassLoader
            classloaderModel.setSelectedItem(classloaderComboBox.getSelectedItem());
        });
    }

}
