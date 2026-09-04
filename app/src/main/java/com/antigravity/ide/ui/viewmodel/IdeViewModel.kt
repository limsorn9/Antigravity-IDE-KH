package com.antigravity.ide.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.ide.data.ai.AiResponseResult
import com.antigravity.ide.data.ai.GeminiService
import com.antigravity.ide.data.model.ChatMessage
import com.antigravity.ide.data.model.CodeApplyMode
import com.antigravity.ide.data.model.EditorTab
import com.antigravity.ide.data.model.FileItem
import com.antigravity.ide.data.model.FileType
import com.antigravity.ide.data.model.MessageSender
import com.antigravity.ide.data.preferences.IdeSettings
import com.antigravity.ide.data.preferences.SettingsManager
import com.antigravity.ide.data.repository.FileManager
import com.antigravity.ide.ui.components.EditorController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdeUiState(
    val rootWorkspace: FileItem? = null,
    val workspaceName: String = "Antigravity Project",
    val openTabs: List<EditorTab> = emptyList(),
    val activeTabId: String? = null,
    val isExplorerVisible: Boolean = true,
    val isAiPanelVisible: Boolean = true,
    val isSettingsDialogVisible: Boolean = false,
    val isPreviewVisible: Boolean = false,
    val isAiGenerating: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val statusMessage: String? = null,
    val settings: IdeSettings = IdeSettings()
) {
    val activeTab: EditorTab?
        get() = openTabs.firstOrNull { it.id == activeTabId }
}

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val fileManager = FileManager(application.applicationContext)
    private val settingsManager = SettingsManager(application.applicationContext)
    private val geminiService = GeminiService()

    val editorController = EditorController()

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    // Cache for sample in-memory files when running without SAF picked directory
    private val inMemoryFiles = mutableMapOf<String, String>()

    init {
        // Collect settings
        viewModelScope.launch {
            settingsManager.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }

        // Initialize with rich sample project
        loadDefaultSampleProject()

        // Welcome message from AI
        addAiMessage(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                content = "👋 Welcome to **Antigravity IDE**!\n\nI am your native AI coding companion. Ask me to write code, debug issues, or refactor functions.\n\nWhenever I generate code, you can tap **Apply to Editor** or **Insert at Cursor** to directly mutate the code without copy-pasting!"
            )
        )
    }

    private fun loadDefaultSampleProject() {
        val (root, contents) = fileManager.createSampleWorkspace()
        inMemoryFiles.clear()
        inMemoryFiles.putAll(contents)

        _uiState.update {
            it.copy(
                rootWorkspace = root,
                workspaceName = root.name
            )
        }

        // Open the Main.kt sample file by default
        root.children.firstOrNull()?.let { openFile(it) }
    }

    fun openWorkspaceTree(treeUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Loading workspace...") }
            val result = fileManager.loadTree(treeUri)
            result.onSuccess { root ->
                _uiState.update {
                    it.copy(
                        rootWorkspace = root,
                        workspaceName = root.name,
                        openTabs = emptyList(),
                        activeTabId = null,
                        statusMessage = "Workspace loaded: ${root.name}"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(statusMessage = "Failed to open directory: ${err.message}")
                }
            }
        }
    }

    fun openFile(fileItem: FileItem) {
        if (fileItem.isDirectory) return

        // Check if already open
        val existingTab = _uiState.value.openTabs.firstOrNull { it.fileItem.id == fileItem.id }
        if (existingTab != null) {
            _uiState.update { it.copy(activeTabId = existingTab.id) }
            editorController.setText(existingTab.content)
            return
        }

        viewModelScope.launch {
            val content = if (fileItem.uri != null) {
                fileManager.readFileContent(fileItem.uri).getOrDefault("// Unable to load content")
            } else {
                inMemoryFiles[fileItem.id] ?: "// New file"
            }

            val newTab = EditorTab(
                id = fileItem.id,
                fileItem = fileItem,
                content = content,
                originalContent = content
            )

            _uiState.update {
                it.copy(
                    openTabs = it.openTabs + newTab,
                    activeTabId = newTab.id
                )
            }
            editorController.setText(content)
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val remainingTabs = state.openTabs.filter { it.id != tabId }
            val newActiveId = if (state.activeTabId == tabId) {
                remainingTabs.lastOrNull()?.id
            } else {
                state.activeTabId
            }
            state.copy(
                openTabs = remainingTabs,
                activeTabId = newActiveId
            )
        }
        _uiState.value.activeTab?.let {
            editorController.setText(it.content)
        }
    }

    fun selectTab(tabId: String) {
        _uiState.update { it.copy(activeTabId = tabId) }
        _uiState.value.activeTab?.let {
            editorController.setText(it.content)
        }
    }

    fun onEditorContentChanged(newContent: String) {
        val activeId = _uiState.value.activeTabId ?: return
        _uiState.update { state ->
            val updatedTabs = state.openTabs.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        content = newContent,
                        isModified = newContent != tab.originalContent
                    )
                } else tab
            }
            state.copy(openTabs = updatedTabs)
        }
    }

    fun onCursorChanged(line: Int, column: Int, selectedText: String) {
        val activeId = _uiState.value.activeTabId ?: return
        _uiState.update { state ->
            val updatedTabs = state.openTabs.map { tab ->
                if (tab.id == activeId) {
                    tab.copy(
                        cursorLine = line,
                        cursorColumn = column,
                        selectedText = selectedText
                    )
                } else tab
            }
            state.copy(openTabs = updatedTabs)
        }
    }

    fun saveCurrentFile() {
        val activeTab = _uiState.value.activeTab ?: return
        viewModelScope.launch {
            if (activeTab.fileItem.uri != null) {
                fileManager.writeFileContent(activeTab.fileItem.uri, activeTab.content)
                    .onSuccess {
                        markTabSaved(activeTab.id)
                        _uiState.update { it.copy(statusMessage = "Saved ${activeTab.title}") }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(statusMessage = "Error saving: ${e.message}") }
                    }
            } else {
                inMemoryFiles[activeTab.fileItem.id] = activeTab.content
                markTabSaved(activeTab.id)
                _uiState.update { it.copy(statusMessage = "Saved ${activeTab.title} (in-memory)") }
            }
        }
    }

    private fun markTabSaved(tabId: String) {
        _uiState.update { state ->
            val updatedTabs = state.openTabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(originalContent = tab.content, isModified = false)
                } else tab
            }
            state.copy(openTabs = updatedTabs)
        }
    }

    fun createNewFile(fileName: String) {
        val root = _uiState.value.rootWorkspace ?: return
        viewModelScope.launch {
            if (root.uri != null) {
                fileManager.createFile(root.uri, fileName).onSuccess { newFile ->
                    refreshWorkspace()
                    openFile(newFile)
                }
            } else {
                // In-memory demo mode
                val newId = "file_${System.currentTimeMillis()}"
                val newFile = FileItem(
                    id = newId,
                    name = fileName,
                    uri = null,
                    isDirectory = false,
                    fileType = FileType.fromFileName(fileName)
                )
                inMemoryFiles[newId] = "// $fileName created in Antigravity IDE\n"
                _uiState.update { state ->
                    val updatedChildren = (state.rootWorkspace?.children ?: emptyList()) + newFile
                    state.copy(
                        rootWorkspace = state.rootWorkspace?.copy(children = updatedChildren)
                    )
                }
                openFile(newFile)
            }
        }
    }

    fun refreshWorkspace() {
        val rootUri = _uiState.value.rootWorkspace?.uri ?: return
        viewModelScope.launch {
            fileManager.loadTree(rootUri).onSuccess { updatedRoot ->
                _uiState.update { it.copy(rootWorkspace = updatedRoot) }
            }
        }
    }

    // --- Seamless AI Agent Integration ---

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val activeTab = _uiState.value.activeTab
        val fileName = activeTab?.title ?: "No File Open"
        val content = activeTab?.content ?: ""
        val selectedText = activeTab?.selectedText

        // Add user message
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            content = prompt
        )
        addAiMessage(userMsg)

        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true) }

            val result = geminiService.generateCodeEdit(
                settings = _uiState.value.settings,
                fileName = fileName,
                fileContent = content,
                selectedText = selectedText,
                userPrompt = prompt
            )

            _uiState.update { it.copy(isAiGenerating = false) }

            when (result) {
                is AiResponseResult.Success -> {
                    val assistantMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        content = result.fullText,
                        codeBlocks = result.codeBlocks
                    )
                    addAiMessage(assistantMsg)
                }
                is AiResponseResult.Error -> {
                    addAiMessage(
                        ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            content = "⚠️ ${result.message}",
                            error = result.message
                        )
                    )
                }
            }
        }
    }

    fun triggerQuickFix() {
        val activeTab = _uiState.value.activeTab ?: return
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            content = "⚡ Auto-Fix & Refactor current file: ${activeTab.title}"
        )
        addAiMessage(userMsg)

        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true) }

            val result = geminiService.generateCodeEdit(
                settings = _uiState.value.settings,
                fileName = activeTab.title,
                fileContent = activeTab.content,
                selectedText = activeTab.selectedText,
                userPrompt = "Find and fix bugs, errors, and optimize the code.",
                isRefactorOrFix = true
            )

            _uiState.update { it.copy(isAiGenerating = false) }

            when (result) {
                is AiResponseResult.Success -> {
                    addAiMessage(
                        ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            content = "I have analyzed and optimized `${activeTab.title}`:\n\n${result.fullText}",
                            codeBlocks = result.codeBlocks
                        )
                    )
                    // If there's an unambiguous primary replacement, offer 1-tap apply
                }
                is AiResponseResult.Error -> {
                    addAiMessage(
                        ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            content = "⚠️ ${result.message}",
                            error = result.message
                        )
                    )
                }
            }
        }
    }

    /**
     * Directly manipulates the native code editor without copy-pasting.
     */
    fun applyAiCodeToEditor(code: String, mode: CodeApplyMode) {
        when (mode) {
            CodeApplyMode.REPLACE_ENTIRE_FILE -> {
                editorController.replaceAllContent(code)
                onEditorContentChanged(code)
                _uiState.update { it.copy(statusMessage = "Applied AI code to entire file") }
            }
            CodeApplyMode.INSERT_AT_CURSOR -> {
                editorController.insertAtCursor(code)
                onEditorContentChanged(editorController.getText())
                _uiState.update { it.copy(statusMessage = "Inserted AI code at cursor") }
            }
            CodeApplyMode.REPLACE_SELECTION -> {
                editorController.replaceSelection(code)
                onEditorContentChanged(editorController.getText())
                _uiState.update { it.copy(statusMessage = "Replaced selection with AI code") }
            }
        }
    }

    private fun addAiMessage(msg: ChatMessage) {
        _uiState.update { it.copy(chatMessages = it.chatMessages + msg) }
    }

    fun toggleExplorer() {
        _uiState.update { it.copy(isExplorerVisible = !it.isExplorerVisible) }
    }

    fun toggleAiPanel() {
        _uiState.update { it.copy(isAiPanelVisible = !it.isAiPanelVisible) }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isSettingsDialogVisible = visible) }
    }

    fun setPreviewVisible(visible: Boolean) {
        _uiState.update { it.copy(isPreviewVisible = visible) }
    }

    fun updateApiKey(key: String) {
        settingsManager.updateApiKey(key)
    }

    fun updateModel(model: String) {
        settingsManager.updateModel(model)
    }

    fun updateFontSize(size: Float) {
        settingsManager.updateFontSize(size)
    }
}
