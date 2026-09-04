package com.antigravity.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.data.model.EditorTab
import com.antigravity.ide.ui.theme.AmberWarning
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.DeepSpace
import com.antigravity.ide.ui.theme.NeonIndigo
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.TextMuted
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary

@Composable
fun IdeTabBar(
    tabs: List<EditorTab>,
    activeTabId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(SurfaceDark)
            .horizontalScroll(rememberScrollState())
            .border(1.dp, BorderSubtle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tabs.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Files Open (Select from Explorer)",
                    color = TextMuted,
                    fontSize = 11.5.sp
                )
            }
        } else {
            tabs.forEach { tab ->
                val isActive = tab.id == activeTabId
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .background(if (isActive) DeepSpace else SurfaceDark)
                        .clickable { onSelectTab(tab.id) }
                        .padding(start = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active top highlight line indicator
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = if (isActive) CyberCyan else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = tab.title,
                        color = if (isActive) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Modified indicator or Close Button
                    if (tab.isModified) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AmberWarning)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = { onCloseTab(tab.id) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Divider between tabs
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .width(1.dp)
                            .background(BorderSubtle)
                    )
                }
            }
        }
    }
}
