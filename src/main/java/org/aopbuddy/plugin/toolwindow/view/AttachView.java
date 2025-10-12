package org.aopbuddy.plugin.toolwindow.view;

import lombok.Getter;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.toolwindow.StatusChangeListener;
import org.aopbuddy.plugin.toolwindow.component.JvmProcessSelectorDialog;
import org.aopbuddy.plugin.toolwindow.model.AttachModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AttachView implements StatusChangeListener<Boolean> {
    @Getter
    private JButton attachButton;

    private final AttachModel attachModel;

    public AttachView(AttachModel model) {
        this.attachModel = model;
        attachButton = new JButton("1.未连接VJM");
        attachButton.setContentAreaFilled(false);
        attachButton.setPreferredSize(new Dimension(200, 30));
        attachButton.addActionListener(e -> {
            List<String> jvms = attachModel.getJvms();
            HttpServer httpServer = JvmProcessSelectorDialog.showAndGetSync(jvms);
            attachModel.startHeartBeat(httpServer);
        });
        attachModel.addStatusChangeListener(this);
    }

    @Override
    public void onStatusChanged(Boolean newStatus) {
        attachButton.setEnabled(!newStatus);
        attachButton.setText(newStatus ? "1.已连接VJM" : "1.未连接VJM");
    }
}