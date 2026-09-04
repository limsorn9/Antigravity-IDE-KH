package com.antigravity.ide.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.data.model.ChatMessage
import com.antigravity.ide.data.model.CodeApplyMode
import com.antigravity.ide.data.model.ExtractedCodeBlock
import com.antigravity.ide.data.model.MessageSender
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.DeepSpace
import com.antigravity.ide.ui.theme.ElectricEmerald
import com.antigravity.ide.ui.theme.NeonIndigo
import com.antigravity.ide.ui.theme.NeonPurple
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.SurfaceVariantDark
import com.antigravity.ide.ui.theme.TextMuted
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary

@Composable
fun AiAssistantPanel(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    modelName: String,
    hasActiveSelection: Boolean,
    onSendMessage: (String) -> Unit,
    onApplyCode: (code: String, mode: CodeApplyMode) -> Unit,
    onClosePanel: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(340.dp)
            .background(SurfaceDark)
            .border(width = 1.dp, color = BorderSubtle)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonIndigo, CyberCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Antigravity AI",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = modelName,
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onClosePanel,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close AI Panel",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PromptChip("⚡ Refactor") { onSendMessage("Refactor this code to follow clean architecture and best practices.") }
            PromptChip("🐛 Fix Bugs") { onSendMessage("Check this code for subtle bugs or runtime exceptions and fix them.") }
            PromptChip("📝 Docs") { onSendMessage("Add clear KDoc / docstrings explaining every function and parameters.") }
            PromptChip("💡 Optimize") { onSendMessage("Optimize this code for maximum execution speed and memory efficiency.") }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSubtle)
        )

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    hasActiveSelection = hasActiveSelection,
                    onApplyCode = onApplyCode
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini is analyzing file context & generating code...",
                            color = CyberCyan,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Input bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (hasActiveSelection) "Ask AI about selected code..." else "Ask AI to edit or generate code...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (inputText.isNotBlank()) NeonIndigo else SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    hasActiveSelection: Boolean,
    onApplyCode: (code: String, mode: CodeApplyMode) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "You" else "Antigravity AI",
                color = if (isUser) NeonPurple else CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (isUser) 10.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 10.dp
                    )
                )
                .background(if (isUser) SurfaceElevated else SurfaceVariantDark)
                .border(
                    width = 1.dp,
                    color = if (isUser) NeonIndigo.copy(alpha = 0.5f) else BorderSubtle,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )

                // Render embedded code blocks with direct apply buttons
                if (message.codeBlocks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    message.codeBlocks.forEach { block ->
                        CodeBlockCard(
                            block = block,
                            hasActiveSelection = hasActiveSelection,
                            onApplyCode = onApplyCode
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    block: ExtractedCodeBlock,
    hasActiveSelection: Boolean,
    onApplyCode: (code: String, mode: CodeApplyMode) -> Unit
) {
    var appliedState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DeepSpace)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
    ) {
        // Code header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = block.language.uppercase(),
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            if (appliedState != null) {
                Text(
                    text = "✓ $appliedState",
                    color = ElectricEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Code snippet preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = block.code,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp,
                maxLines = 14,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Direct AI Action Buttons - NO COPY PASTE NEEDED!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark.copy(alpha = 0.6f))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Apply to entire file
            OutlinedButton(
                onClick = {
                    onApplyCode(block.code, CodeApplyMode.REPLACE_ENTIRE_FILE)
                    appliedState = "Applied to File"
                },
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = NeonIndigo,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply All", fontSize = 10.5.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            // Insert at cursor
            OutlinedButton(
                onClick = {
                    onApplyCode(block.code, CodeApplyMode.INSERT_AT_CURSOR)
                    appliedState = "Inserted at Cursor"
                },
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Input,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Insert Cursor", fontSize = 10.5.sp, color = TextPrimary)
            }

            if (hasActiveSelection) {
                // Replace selection
                OutlinedButton(
                    onClick = {
                        onApplyCode(block.code, CodeApplyMode.REPLACE_SELECTION)
                        appliedState = "Replaced Selection"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = ElectricEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Replace Sel", fontSize = 10.5.sp, color = TextPrimary)
                }
            }
        }
    }
}
