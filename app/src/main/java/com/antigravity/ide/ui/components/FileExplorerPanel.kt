package com.antigravity.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.data.model.FileItem
import com.antigravity.ide.data.model.FileType
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.NeonIndigo
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.SurfaceVariantDark
import com.antigravity.ide.ui.theme.TextMuted
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary

@Composable
fun FileExplorerPanel(
    modifier: Modifier = Modifier,
    rootItem: FileItem?,
    workspaceName: String,
    activeFileId: String?,
    onFileClick: (FileItem) -> Unit,
    onCreateNewFile: (String) -> Unit,
    onOpenFolderRequested: () -> Unit
) {
    var isNewFileDialogOpen by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(SurfaceDark)
            .padding(vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Workspace",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = workspaceName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(
                    onClick = { isNewFileDialogOpen = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New File",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSubtle)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // File List
        if (rootItem != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(rootItem.children) { item ->
                    FileTreeRow(
                        item = item,
                        depth = 0,
                        activeFileId = activeFileId,
                        onFileClick = onFileClick
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Folder Open",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Open Folder Action Button at Bottom
        Button(
            onClick = onOpenFolderRequested,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Open Device Folder",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // New File Dialog
    if (isNewFileDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewFileDialogOpen = false },
            title = { Text(text = "Create New File", color = TextPrimary, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. script.py, helper.kt", color = TextMuted) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onCreateNewFile(newFileName.trim())
                            newFileName = ""
                            isNewFileDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewFileDialogOpen = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceVariantDark
        )
    }
}

@Composable
private fun FileTreeRow(
    item: FileItem,
    depth: Int,
    activeFileId: String?,
    onFileClick: (FileItem) -> Unit
) {
    var isExpanded by remember { mutableStateOf(item.isExpanded) }
    val isSelected = item.id == activeFileId

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) SurfaceElevated else Color.Transparent)
                .clickable {
                    if (item.isDirectory) {
                        isExpanded = !isExpanded
                    } else {
                        onFileClick(item)
                    }
                }
                .padding(
                    start = (12 + depth * 14).dp,
                    top = 6.dp,
                    bottom = 6.dp,
                    end = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = getFileIcon(item.fileType),
                    contentDescription = null,
                    tint = getFileIconTint(item.fileType),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.name,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.isDirectory && isExpanded) {
            item.children.forEach { child ->
                FileTreeRow(
                    item = child,
                    depth = depth + 1,
                    activeFileId = activeFileId,
                    onFileClick = onFileClick
                )
            }
        }
    }
}

private fun getFileIcon(fileType: FileType): ImageVector {
    return when (fileType) {
        FileType.KOTLIN, FileType.JAVA, FileType.JAVASCRIPT, FileType.TYPESCRIPT -> Icons.Default.Code
        FileType.PYTHON, FileType.XML, FileType.JSON -> Icons.Default.Terminal
        FileType.HTML, FileType.CSS -> Icons.Default.Html
        FileType.MARKDOWN -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

private fun getFileIconTint(fileType: FileType): Color {
    return when (fileType) {
        FileType.KOTLIN -> Color(0xFF7F52FF)
        FileType.JAVA -> Color(0xFFF89820)
        FileType.PYTHON -> Color(0xFF3776AB)
        FileType.JAVASCRIPT -> Color(0xFFF7DF1E)
        FileType.TYPESCRIPT -> Color(0xFF3178C6)
        FileType.HTML -> Color(0xFFE34F26)
        FileType.CSS -> Color(0xFF1572B6)
        FileType.JSON -> Color(0xFFFBBF24)
        FileType.MARKDOWN -> Color(0xFF00E5FF)
        else -> TextMuted
    }
}
