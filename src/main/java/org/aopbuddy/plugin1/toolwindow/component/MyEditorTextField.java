package org.aopbuddy.plugin1.toolwindow.component;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;

import org.jetbrains.annotations.NotNull;

public class MyEditorTextField extends EditorTextField {


    public MyEditorTextField(@NotNull Project project, @NotNull FileType fileType) {
        super(null, project, fileType, false, false);
    }


    public static void setupTextFieldEditor(@NotNull EditorEx editor) {
        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(true);
        settings.setLineNumbersShown(true);
        settings.setIndentGuidesShown(true);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        editor.getScrollPane().setBorder(JBUI.Borders.empty());
    }

    protected EditorEx createEditor() {
        EditorEx editor = WriteAction.compute(() -> super.createEditor());
        initOneLineModePre(editor);
        setupTextFieldEditor(editor);
        return editor;
    }

//    public void repaint(long tm, int x, int y, int width, int height) {
//        super.repaint(tm, x, y, width, height);
//        if (getEditor() instanceof EditorEx)
//            initOneLineModePre((EditorEx) getEditor());
//    }

    public void setBorder(Border border) {
        super.setBorder(JBUI.Borders.empty());
    }


    private void initOneLineModePre(@NotNull EditorEx editor) {
        editor.setOneLineMode(false);
        editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null));
        editor.getSettings().setCaretRowShown(false);
    }
}