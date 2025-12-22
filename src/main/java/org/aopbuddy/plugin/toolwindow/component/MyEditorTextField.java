package org.aopbuddy.plugin.toolwindow.component;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.JBUI;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


  public void setBorder(Border border) {
    super.setBorder(JBUI.Borders.empty());
  }


  private void initOneLineModePre(@NotNull EditorEx editor) {
    editor.setOneLineMode(false);
    editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null));
    editor.getSettings().setCaretRowShown(false);
  }

  public void setText(@Nullable String text, @NotNull FileType fileType) {
    Document document = createDocument(text, fileType);
    setNewDocumentAndFileType(fileType, document);
    PsiFile psiFile = ReadAction.compute(
        () -> PsiDocumentManager.getInstance(getProject()).getPsiFile(document));
    if (psiFile != null) {
      WriteCommandAction.runWriteCommandAction(getProject(),
          (Computable<PsiElement>) () -> CodeStyleManager.getInstance(getProject())
              .reformat(psiFile));
    }
  }

  public Document createDocument(@Nullable String text, @NotNull FileType fileType) {
    PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
    long stamp = LocalTimeCounter.currentTime();
    PsiFile psiFile = factory.createFileFromText("aop-plugin", fileType,
        (text == null) ? "" : text, stamp, true, false);
    return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
  }
}