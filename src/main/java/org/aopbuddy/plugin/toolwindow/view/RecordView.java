package org.aopbuddy.plugin.toolwindow.view;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import lombok.Getter;
import org.aopbuddy.plugin.toolwindow.component.RecordConfigDialog;
import org.aopbuddy.plugin.toolwindow.model.RecordModel;
import org.aopbuddy.plugin.toolwindow.model.RunResultModel;

import javax.swing.*;
import java.awt.*;

public class RecordView {
    @Getter
    private JToggleButton recordButton;
    private RunResultModel runResultModel;
    private final RecordModel recordModel;

    public RecordView(RecordModel recordModel, RunResultModel runResultModel) {
        this.recordModel = recordModel;
        this.runResultModel = runResultModel;
        this.recordButton = createRecordToggleButton();
    }

    private JToggleButton createRecordToggleButton() {
        JToggleButton button = new JToggleButton();
        button.setIcon(AllIcons.Actions.StartDebugger); // 使用录制图标
        button.setSelectedIcon(AllIcons.Actions.Suspend); // 录制中使用暂停图标
        button.setContentAreaFilled(false);
        button.setToolTipText("录制数据");
        button.setPreferredSize(new Dimension(30, 30));
        button.addActionListener(e -> {
            if (recordButton.isSelected()) {
                if (!recordModel.isAttached()) {
                    JOptionPane.showMessageDialog(null, "请先连接到目标服务器", "警告", JOptionPane.WARNING_MESSAGE);
                    recordButton.setSelected(false);
                    return;
                }
                handleStartRecording();
            } else {
                handleStopRecording();
            }
        });

        return button;
    }

    private void handleStartRecording() {
        RecordConfigDialog dialog = new RecordConfigDialog();
        if (!dialog.showAndGet()) {
            recordButton.setSelected(false);
            return;
        }

        // 获取对话框输入值并开始录制
        recordModel.startRecording(dialog.getClassName(), dialog.getMethodName());

        // 更新UI状态
        recordButton.setToolTipText("停止录制");
        recordButton.setBackground(JBColor.GREEN);
        runResultModel.setStatus("录制中... 开始时间: " + recordModel.getFormattedStartTime());
    }

    private void handleStopRecording() {
        // 停止录制
        recordModel.stopRecording();

        // 更新UI状态
        recordButton.setToolTipText("录制数据");
        recordButton.setBackground(null);
        runResultModel.setStatus(String.format("录制已停止，时长: %s", recordModel.getRecordingDuration()));
    }

}