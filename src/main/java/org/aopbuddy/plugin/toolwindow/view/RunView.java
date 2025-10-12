package org.aopbuddy.plugin.toolwindow.view;

import com.intellij.icons.AllIcons;
import lombok.Getter;
import org.aopbuddy.plugin.toolwindow.model.RunModel;

import javax.swing.*;
import java.awt.*;

public class RunView {
    @Getter
    private JButton runButton;

    private final RunModel runModel;

    public RunView(RunModel model) {
        this.runModel = model;
        runButton = new JButton();
        runButton.setIcon(AllIcons.Actions.RunAll);
        runButton.setContentAreaFilled(false);
        runButton.setToolTipText("执行");
        runButton.setPreferredSize(new Dimension(30, 30));
        runButton.addActionListener(e -> runModel.eval());
    }
}