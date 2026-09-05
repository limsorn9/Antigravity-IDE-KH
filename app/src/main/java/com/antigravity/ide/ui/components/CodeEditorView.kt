package com.antigravity.ide.ui.components

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.ide.data.model.FileType
import com.antigravity.ide.ui.theme.DeepSpace
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent

/**
 * Controller class to programmatically interact with the native Sora CodeEditor.
 * Enables the AI agent to replace, insert, format, and inspect code directly.
 */
class EditorController {
    internal var internalEditor: CodeEditor? = null

    fun setText(newText: String) {
        internalEditor?.let { editor ->
            if (editor.text.toString() != newText) {
                editor.setText(newText)
            }
        }
    }

    fun getText(): String {
        return internalEditor?.text?.toString() ?: ""
    }

    fun replaceSelection(replacement: String) {
        internalEditor?.let { editor ->
            val cursor = editor.cursor
            if (cursor.isSelected) {
                editor.text.replace(
                    cursor.leftLine,
                    cursor.leftColumn,
                    cursor.rightLine,
                    cursor.rightColumn,
                    replacement
                )
            } else {
                // If nothing selected, insert at current cursor
                insertAtCursor(replacement)
            }
        }
    }

    fun insertAtCursor(textToInsert: String) {
        internalEditor?.let { editor ->
            val cursor = editor.cursor
            editor.text.insert(cursor.leftLine, cursor.leftColumn, textToInsert)
        }
    }

    fun replaceAllContent(newContent: String) {
        internalEditor?.setText(newContent)
    }

    fun getSelectedText(): String {
        val editor = internalEditor ?: return ""
        val cursor = editor.cursor
        return if (cursor.isSelected) {
            val start = cursor.left
            val end = cursor.right
            val fullText = editor.text.toString()
            if (start in 0..end && end <= fullText.length) {
                fullText.substring(start, end)
            } else ""
        } else ""
    }

    fun undo() {
        if (internalEditor?.canUndo() == true) {
            internalEditor?.undo()
        }
    }

    fun redo() {
        if (internalEditor?.canRedo() == true) {
            internalEditor?.redo()
        }
    }

    fun setFontSize(sizeSp: Float) {
        internalEditor?.setTextSize(sizeSp)
    }

    fun setWordWrap(wrap: Boolean) {
        internalEditor?.isWordwrap = wrap
    }

    fun setTabWidth(spaces: Int) {
        internalEditor?.tabWidth = spaces
    }
}

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    content: String,
    fileType: FileType,
    fontSizeSp: Float = 14f,
    wordWrap: Boolean = false,
    tabSize: Int = 4,
    controller: EditorController = remember { EditorController() },
    onContentChange: (String) -> Unit = {},
    onCursorChange: (line: Int, column: Int, selectedText: String) -> Unit = { _, _, _ -> }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                CodeEditor(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Visual configurations
                    colorScheme = SchemeDarcula()
                    typefaceText = Typeface.MONOSPACE
                    typefaceLineNumber = Typeface.MONOSPACE
                    isLineNumberEnabled = true
                    setTextSize(fontSizeSp)
                    isWordwrap = wordWrap
                    tabWidth = tabSize
                    setPinLineNumber(true)

                    // Set initial text
                    setText(content)

                    // Text change listener
                    subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                        val currentText = text.toString()
                        onContentChange(currentText)
                    }

                    // Selection / cursor listener
                    subscribeEvent(SelectionChangeEvent::class.java) { _, _ ->
                        val line = cursor.leftLine + 1
                        val column = cursor.leftColumn + 1
                        val selected = if (cursor.isSelected) {
                            val st = cursor.left
                            val en = cursor.right
                            val full = text.toString()
                            if (st in 0..en && en <= full.length) full.substring(st, en) else ""
                        } else ""
                        onCursorChange(line, column, selected)
                    }

                    controller.internalEditor = this
                }
            },
            update = { editor ->
                controller.internalEditor = editor
                if (editor.text.toString() != content) {
                    val prevCursorLine = editor.cursor.leftLine
                    val prevCursorCol = editor.cursor.leftColumn
                    editor.setText(content)
                    // Try to restore cursor position if valid
                    try {
                        editor.setSelection(prevCursorLine, prevCursorCol)
                    } catch (_: Exception) {}
                }
                editor.setTextSize(fontSizeSp)
                editor.isWordwrap = wordWrap
                editor.tabWidth = tabSize
            }
        )
    }

    DisposableEffect(controller) {
        onDispose {
            controller.internalEditor = null
        }
    }
}
