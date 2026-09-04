package com.antigravity.ide.data.ai

import com.antigravity.ide.data.model.ExtractedCodeBlock
import com.antigravity.ide.data.model.MessageSender
import com.antigravity.ide.data.preferences.IdeSettings
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

sealed class AiResponseResult {
    data class Success(
        val fullText: String,
        val codeBlocks: List<ExtractedCodeBlock>,
        val primaryCodeReplacement: String? = null
    ) : AiResponseResult()

    data class Error(val message: String) : AiResponseResult()
}

class GeminiService {

    private fun getModel(settings: IdeSettings): GenerativeModel {
        val apiKey = settings.geminiApiKey.ifBlank { "DEMO_KEY_MOCK" }
        val modelName = settings.geminiModel.ifBlank { "gemini-1.5-flash" }

        val systemPrompt = """
            You are Antigravity AI, an expert Android-native AI coding assistant integrated directly into the Antigravity IDE for Android.
            Your job is to read file code context, understand user instructions, and provide clean, production-ready code.
            
            RULES:
            1. When writing code, ALWAYS enclose code inside triple-backtick markdown blocks with the correct language tag (e.g. ```kotlin ... ``` or ```python ... ```).
            2. For direct code modifications or refactoring requests, ensure the code block contains complete, valid drop-in code so the IDE can automatically inject or replace it in the user's editor.
            3. Be concise and helpful. Explain key architectural decisions briefly.
        """.trimIndent()

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                topK = 32
                topP = 0.95f
            },
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text(systemPrompt)
            }
        )
    }

    /**
     * Executes an AI request given the user prompt, open file content, language, and selection context.
     */
    suspend fun generateCodeEdit(
        settings: IdeSettings,
        fileName: String,
        fileContent: String,
        selectedText: String?,
        userPrompt: String,
        isRefactorOrFix: Boolean = false
    ): AiResponseResult = withContext(Dispatchers.IO) {
        if (settings.geminiApiKey.isBlank()) {
            return@withContext AiResponseResult.Error(
                "Gemini API Key is missing. Please tap the Settings icon in the top bar to enter your free Google Gemini API Key from Google AI Studio."
            )
        }

        try {
            val model = getModel(settings)
            val promptBuilder = StringBuilder()

            promptBuilder.append("Active File: ").append(fileName).append("\n\n")

            if (!selectedText.isNullOrBlank()) {
                promptBuilder.append("--- USER SELECTED CODE SNIPPET ---\n")
                promptBuilder.append("```\n").append(selectedText).append("\n```\n\n")
            }

            promptBuilder.append("--- FULL FILE CONTENT ---\n")
            promptBuilder.append("```\n").append(fileContent).append("\n```\n\n")

            if (isRefactorOrFix) {
                promptBuilder.append("TASK: Analyze the code above, detect potential bugs, performance bottlenecks, or style improvements, and provide an optimized, fixed version of the code that can be directly applied to the editor.\n")
                if (userPrompt.isNotBlank()) {
                    promptBuilder.append("Specific User Notes: ").append(userPrompt).append("\n")
                }
            } else {
                promptBuilder.append("USER INSTRUCTION: ").append(userPrompt).append("\n")
                promptBuilder.append("Please output the exact modified code inside a code block that can be directly applied to the file.\n")
            }

            val response = model.generateContent(promptBuilder.toString())
            val text = response.text ?: "No response received from Gemini."

            val codeBlocks = extractCodeBlocks(text)
            val primaryReplacement = codeBlocks.firstOrNull()?.code

            AiResponseResult.Success(
                fullText = text,
                codeBlocks = codeBlocks,
                primaryCodeReplacement = primaryReplacement
            )
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: e.message ?: "Unknown error calling Gemini API"
            AiResponseResult.Error(errorMsg)
        }
    }

    /**
     * Helper to extract ```language ... ``` code blocks from markdown text.
     */
    fun extractCodeBlocks(markdownText: String): List<ExtractedCodeBlock> {
        val blocks = mutableListOf<ExtractedCodeBlock>()
        val regex = Pattern.compile("```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)```")
        val matcher = regex.matcher(markdownText)

        while (matcher.find()) {
            val lang = matcher.group(1)?.trim() ?: "plaintext"
            val code = matcher.group(2) ?: ""
            blocks.add(
                ExtractedCodeBlock(
                    language = if (lang.isEmpty()) "code" else lang,
                    code = code.trimEnd()
                )
            )
        }
        return blocks
    }
}
