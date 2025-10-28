package org.aopbuddy.plugin.toolwindow.view;

import lombok.Getter;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.toolwindow.component.JvmProcessSelectorDialog;
import org.aopbuddy.plugin.toolwindow.model.AttachModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AttachView {
    @Getter
    private JButton attachButton;

    private final AttachModel attachModel;

    public AttachView(AttachModel model) {
        this.attachModel = model;
        attachButton = new JButton("1.未连接JVM");
        attachButton.setContentAreaFilled(false);
        attachButton.setPreferredSize(new Dimension(200, 30));
        attachButton.setModel(attachModel);
        attachButton.addActionListener(e -> {
            List<String> jvms = attachModel.getJvms();
            HttpServer httpServer = JvmProcessSelectorDialog.showAndGetSync(jvms);
            attachModel.startHeartBeat(httpServer);
        });
        attachModel.addChangeListener(e -> {
            attachButton.setText(attachModel.isAttached() ? "2.已连接JVM" : "1.未连接JVM");
        });
    }
}