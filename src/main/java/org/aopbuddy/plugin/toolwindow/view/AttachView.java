package org.aopbuddy.plugin.toolwindow.view;

import lombok.Getter;
import org.aopbuddy.plugin.infra.model.HttpServer;
import org.aopbuddy.plugin.infra.util.I18nUtil;
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
    attachButton = new JButton(I18nUtil.message("attach.view.status.disconnected"));
    attachButton.setContentAreaFilled(false);
    attachButton.setPreferredSize(new Dimension(200, 30));
    attachButton.setModel(attachModel);
    attachButton.addActionListener(e -> {
      List<String> jvms = attachModel.getJvms();
      HttpServer httpServer = JvmProcessSelectorDialog.showAndGetSync(jvms);
      attachModel.startHeartBeat(httpServer);

    });
    attachModel.addChangeListener(e -> {
      attachButton.setText(
          attachModel.isAttached() ? I18nUtil.message("attach.view.status.connected")
              : I18nUtil.message("attach.view.status.disconnected"));
    });
  }

  public void dispose() {
    if (attachButton != null) {
      // 断开Model引用，防止内存泄漏
      attachButton.setModel(new DefaultButtonModel());
      for (java.awt.event.ActionListener al : attachButton.getActionListeners()) {
        attachButton.removeActionListener(al);
      }
    }
  }
}