package com.antigravity.ide.data.model

import android.net.Uri

enum class FileType(val extension: String, val languageName: String) {
    KOTLIN("kt", "kotlin"),
    JAVA("java", "java"),
    PYTHON("py", "python"),
    JAVASCRIPT("js", "javascript"),
    TYPESCRIPT("ts", "typescript"),
    HTML("html", "html"),
    CSS("css", "css"),
    JSON("json", "json"),
    MARKDOWN("md", "markdown"),
    XML("xml", "xml"),
    TEXT("txt", "plaintext"),
    UNKNOWN("", "plaintext");

    companion object {
        fun fromExtension(ext: String): FileType {
            val normalized = ext.lowercase().trimStart('.')
            return entries.firstOrNull { it.extension.equals(normalized, ignoreCase = true) } ?: UNKNOWN
        }

        fun fromFileName(name: String): FileType {
            val ext = name.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}

data class FileItem(
    val id: String,
    val name: String,
    val uri: Uri?,
    val isDirectory: Boolean,
    val children: List<FileItem> = emptyList(),
    val fileType: FileType = FileType.fromFileName(name),
    val isExpanded: Boolean = false,
    val sizeBytes: Long = 0,
    val parentId: String? = null
)

data class EditorTab(
    val id: String,
    val fileItem: FileItem,
    val title: String = fileItem.name,
    val content: String,
    val originalContent: String = content,
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val selectedText: String = "",
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0
)
