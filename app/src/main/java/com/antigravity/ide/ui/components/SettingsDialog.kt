package com.antigravity.ide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.ide.data.preferences.IdeSettings
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
fun SettingsDialog(
    currentSettings: IdeSettings,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveModel: (String) -> Unit,
    onSaveFontSize: (Float) -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentSettings.geminiApiKey) }
    var selectedModel by remember { mutableStateOf(currentSettings.geminiModel) }
    var fontSize by remember { mutableFloatStateOf(currentSettings.fontSizeSp) }

    val supportedModels = listOf(
        "gemini-1.5-flash",
        "gemini-1.5-pro",
        "gemini-2.0-flash-exp"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyberCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "IDE & AI Settings",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gemini API Key Section
                Text(
                    text = "Google Gemini API Key",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    placeholder = { Text("Paste AI Studio API Key...", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = NeonIndigo
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "Free API key from Google AI Studio (aistudio.google.com)",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Model Selection
                Text(
                    text = "Active Gemini Model",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedModels.forEach { model ->
                        val isSelected = selectedModel == model
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonIndigo else SurfaceDark)
                                .clickable { selectedModel = model }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = model.removePrefix("gemini-"),
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Editor Font Size",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${fontSize.toInt()} sp",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 10f..26f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = NeonIndigo,
                        inactiveTrackColor = SurfaceElevated
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveApiKey(apiKeyText)
                    onSaveModel(selectedModel)
                    onSaveFontSize(fontSize)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceVariantDark
    )
}
