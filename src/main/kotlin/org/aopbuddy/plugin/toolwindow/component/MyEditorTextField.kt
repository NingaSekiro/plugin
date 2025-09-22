package org.aopbuddy.plugin.toolwindow.component

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.NotNull

class MyEditorTextField(@NotNull project: Project, @NotNull fileType: FileType) : EditorTextField(null, project, fileType, false, false) {

    companion object {
        @JvmStatic
        fun setupEditor(@NotNull editor: EditorEx) {
            val settings = editor.settings
            settings.isFoldingOutlineShown = true
            settings.isLineNumbersShown = true
            settings.isIndentGuidesShown = true
            editor.scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            editor.scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
            editor.scrollPane.border = JBUI.Borders.empty()
        }
    }

    override fun createEditor(): EditorEx {
        val editor = WriteAction.compute<EditorEx, RuntimeException> { super.createEditor() }
        initOneLineModePre(editor)
        setupEditor(editor)
        return editor
    }

    override fun setBorder(border: javax.swing.border.Border?) {
        super.setBorder(JBUI.Borders.empty())
    }

    private fun initOneLineModePre(@NotNull editor: EditorEx) {
        editor.isOneLineMode = false
        editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null))
        editor.settings.isCaretRowShown = false
    }
}