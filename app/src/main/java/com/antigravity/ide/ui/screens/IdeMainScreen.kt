package com.antigravity.ide.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.data.model.FileType
import com.antigravity.ide.ui.components.AiAssistantPanel
import com.antigravity.ide.ui.components.CodeEditorView
import com.antigravity.ide.ui.components.FileExplorerPanel
import com.antigravity.ide.ui.components.IdeTabBar
import com.antigravity.ide.ui.components.IdeTopBar
import com.antigravity.ide.ui.components.PreviewDialog
import com.antigravity.ide.ui.components.SettingsDialog
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.DeepSpace
import com.antigravity.ide.ui.theme.NeonIndigo
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.TextMuted
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary
import com.antigravity.ide.ui.viewmodel.IdeViewModel

@Composable
fun IdeMainScreen(
    viewModel: IdeViewModel,
    onOpenFolderRequested: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            IdeTopBar(
                isExplorerOpen = uiState.isExplorerVisible,
                isAiOpen = uiState.isAiPanelVisible,
                isCurrentFileModified = uiState.activeTab?.isModified == true,
                onToggleExplorer = { viewModel.toggleExplorer() },
                onToggleAi = { viewModel.toggleAiPanel() },
                onSaveFile = { viewModel.saveCurrentFile() },
                onRunPreview = { viewModel.setPreviewVisible(true) },
                onQuickAiFix = { viewModel.triggerQuickFix() },
                onUndo = { viewModel.editorController.undo() },
                onRedo = { viewModel.editorController.redo() },
                onOpenSettings = { viewModel.setSettingsDialogVisible(true) },
                onOpenFolder = onOpenFolderRequested
            )
        },
        bottomBar = {
            // IDE Status Bar
            IdeStatusBar(
                cursorLine = uiState.activeTab?.cursorLine ?: 1,
                cursorCol = uiState.activeTab?.cursorColumn ?: 1,
                fileType = uiState.activeTab?.fileItem?.fileType ?: FileType.UNKNOWN,
                selectedText = uiState.activeTab?.selectedText ?: "",
                isAiGenerating = uiState.isAiGenerating,
                modelName = uiState.settings.geminiModel
            )
        },
        containerColor = DeepSpace
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left: Collapsible File Explorer Panel
            AnimatedVisibility(
                visible = uiState.isExplorerVisible,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            ) {
                FileExplorerPanel(
                    rootItem = uiState.rootWorkspace,
                    workspaceName = uiState.workspaceName,
                    activeFileId = uiState.activeTab?.fileItem?.id,
                    onFileClick = { fileItem -> viewModel.openFile(fileItem) },
                    onCreateNewFile = { fileName -> viewModel.createNewFile(fileName) },
                    onOpenFolderRequested = onOpenFolderRequested
                )
            }

            // Center: Tab Bar + Code Editor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                IdeTabBar(
                    tabs = uiState.openTabs,
                    activeTabId = uiState.activeTabId,
                    onSelectTab = { tabId -> viewModel.selectTab(tabId) },
                    onCloseTab = { tabId -> viewModel.closeTab(tabId) }
                )

                val activeTab = uiState.activeTab
                if (activeTab != null) {
                    CodeEditorView(
                        modifier = Modifier.weight(1f),
                        content = activeTab.content,
                        fileType = activeTab.fileItem.fileType,
                        fontSizeSp = uiState.settings.fontSizeSp,
                        wordWrap = uiState.settings.wordWrap,
                        tabSize = uiState.settings.tabSize,
                        controller = viewModel.editorController,
                        onContentChange = { newContent ->
                            viewModel.onEditorContentChanged(newContent)
                        },
                        onCursorChange = { line, col, selectedText ->
                            viewModel.onCursorChanged(line, col, selectedText)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(DeepSpace),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🪐 Antigravity IDE",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a file from the explorer or open a project folder to start editing.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Right: Collapsible AI Assistant Panel
            AnimatedVisibility(
                visible = uiState.isAiPanelVisible,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                AiAssistantPanel(
                    messages = uiState.chatMessages,
                    isGenerating = uiState.isAiGenerating,
                    modelName = uiState.settings.geminiModel,
                    hasActiveSelection = !uiState.activeTab?.selectedText.isNullOrBlank(),
                    onSendMessage = { prompt -> viewModel.sendAiPrompt(prompt) },
                    onApplyCode = { code, mode -> viewModel.applyAiCodeToEditor(code, mode) },
                    onClosePanel = { viewModel.toggleAiPanel() }
                )
            }
        }
    }

    // Dialogs
    if (uiState.isSettingsDialogVisible) {
        SettingsDialog(
            currentSettings = uiState.settings,
            onDismiss = { viewModel.setSettingsDialogVisible(false) },
            onSaveApiKey = { key -> viewModel.updateApiKey(key) },
            onSaveModel = { model -> viewModel.updateModel(model) },
            onSaveFontSize = { size -> viewModel.updateFontSize(size) }
        )
    }

    if (uiState.isPreviewVisible) {
        val currentTab = uiState.activeTab
        PreviewDialog(
            fileName = currentTab?.title ?: "Preview",
            content = currentTab?.content ?: "<!-- No content to preview -->",
            fileType = currentTab?.fileItem?.fileType ?: FileType.HTML,
            onDismiss = { viewModel.setPreviewVisible(false) }
        )
    }
}

@Composable
private fun IdeStatusBar(
    cursorLine: Int,
    cursorCol: Int,
    fileType: FileType,
    selectedText: String,
    isAiGenerating: Boolean,
    modelName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Ln $cursorLine, Col $cursorCol",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            if (selectedText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${selectedText.length} chars selected)",
                    color = CyberCyan,
                    fontSize = 10.5.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAiGenerating) {
                Text(
                    text = "⚡ AI Thinking...",
                    color = NeonIndigo,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = "UTF-8",
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = fileType.languageName.uppercase(),
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
