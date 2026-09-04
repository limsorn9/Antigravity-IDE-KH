package com.antigravity.ide.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class CodeApplyMode {
    REPLACE_ENTIRE_FILE,
    INSERT_AT_CURSOR,
    REPLACE_SELECTION
}

data class ExtractedCodeBlock(
    val id: String = UUID.randomUUID().toString(),
    val language: String,
    val code: String,
    val targetFileHint: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val content: String,
    val codeBlocks: List<ExtractedCodeBlock> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val error: String? = null
)
