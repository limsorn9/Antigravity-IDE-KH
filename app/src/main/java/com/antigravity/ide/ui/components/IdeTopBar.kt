package com.antigravity.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.ui.theme.AmberWarning
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.ElectricEmerald
import com.antigravity.ide.ui.theme.NeonIndigo
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary

@Composable
fun IdeTopBar(
    isExplorerOpen: Boolean,
    isAiOpen: Boolean,
    isCurrentFileModified: Boolean,
    onToggleExplorer: () -> Unit,
    onToggleAi: () -> Unit,
    onSaveFile: () -> Unit,
    onRunPreview: () -> Unit,
    onQuickAiFix: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFolder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SurfaceDark)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Sidebar toggle + App Logo & Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleExplorer,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewSidebar,
                    contentDescription = "Toggle Explorer",
                    tint = if (isExplorerOpen) CyberCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Brand Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🪐 Antigravity",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = " IDE",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }
        }

        // Center / Action Toolbar
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Quick AI Fix button
            Button(
                onClick = onQuickAiFix,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(listOf(NeonIndigo, Color(0xFF7928CA)))
                    ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI Fix", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Save Button
            IconButton(
                onClick = onSaveFile,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save File",
                    tint = if (isCurrentFileModified) AmberWarning else ElectricEmerald,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Run / Live Preview Button
            IconButton(
                onClick = onRunPreview,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run Preview",
                    tint = ElectricEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Undo
            IconButton(
                onClick = onUndo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Redo
            IconButton(
                onClick = onRedo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Right: AI Assistant toggle + Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleAi,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Toggle AI Assistant",
                    tint = if (isAiOpen) NeonIndigo else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
