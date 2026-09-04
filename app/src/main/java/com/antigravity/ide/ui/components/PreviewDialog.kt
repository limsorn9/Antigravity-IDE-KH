package com.antigravity.ide.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antigravity.ide.data.model.FileType
import com.antigravity.ide.ui.theme.BorderSubtle
import com.antigravity.ide.ui.theme.CyberCyan
import com.antigravity.ide.ui.theme.DeepSpace
import com.antigravity.ide.ui.theme.SurfaceDark
import com.antigravity.ide.ui.theme.SurfaceElevated
import com.antigravity.ide.ui.theme.TextPrimary
import com.antigravity.ide.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewDialog(
    fileName: String,
    content: String,
    fileType: FileType,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(SurfaceElevated)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Execution: $fileName",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // WebView Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()

                                val htmlToLoad = if (fileType == FileType.HTML) {
                                    content
                                } else if (fileType == FileType.MARKDOWN) {
                                    renderMarkdownHtml(content)
                                } else {
                                    renderCodePreviewHtml(fileName, content)
                                }

                                loadDataWithBaseURL(null, htmlToLoad, "text/html", "utf-8", null)
                            }
                        },
                        update = { webView ->
                            val htmlToLoad = if (fileType == FileType.HTML) {
                                content
                            } else if (fileType == FileType.MARKDOWN) {
                                renderMarkdownHtml(content)
                            } else {
                                renderCodePreviewHtml(fileName, content)
                            }
                            webView.loadDataWithBaseURL(null, htmlToLoad, "text/html", "utf-8", null)
                        }
                    )
                }
            }
        }
    }
}

private fun renderMarkdownHtml(markdown: String): String {
    val escaped = markdown
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: -apple-system, sans-serif; padding: 20px; line-height: 1.6; color: #1e293b; }
                pre { background: #f1f5f9; padding: 12px; border-radius: 6px; overflow-x: auto; }
            </style>
        </head>
        <body>
            <pre>$escaped</pre>
        </body>
        </html>
    """.trimIndent()
}

private fun renderCodePreviewHtml(fileName: String, code: String): String {
    val escaped = code
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { background: #0f172a; color: #f8fafc; font-family: monospace; padding: 16px; font-size: 13px; line-height: 1.5; }
                .title { color: #38bdf8; font-weight: bold; margin-bottom: 12px; }
            </style>
        </head>
        <body>
            <div class="title">// Output preview for $fileName</div>
            <pre><code>$escaped</code></pre>
        </body>
        </html>
    """.trimIndent()
}
