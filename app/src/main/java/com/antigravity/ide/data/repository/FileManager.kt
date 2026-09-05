package com.antigravity.ide.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.antigravity.ide.data.model.FileItem
import com.antigravity.ide.data.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class FileManager(private val context: Context) {

    /**
     * Reads string content from a given Uri.
     */
    suspend fun readFileContent(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    Result.success(reader.readText())
                }
            } ?: Result.failure(Exception("Unable to open input stream for $uri"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Writes string content to a given Uri (overwrite mode).
     */
    suspend fun writeFileContent(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            } ?: return@withContext Result.failure(Exception("Unable to open output stream for $uri"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new file in the specified directory DocumentFile.
     */
    suspend fun createFile(parentUri: Uri, fileName: String, mimeType: String = "text/plain"): Result<FileItem> =
        withContext(Dispatchers.IO) {
            try {
                val parentDoc = DocumentFile.fromTreeUri(context, parentUri)
                    ?: DocumentFile.fromSingleUri(context, parentUri)
                    ?: return@withContext Result.failure(Exception("Parent folder not found"))

                val newDoc = parentDoc.createFile(mimeType, fileName)
                    ?: return@withContext Result.failure(Exception("Failed to create file $fileName"))

                Result.success(
                    FileItem(
                        id = newDoc.uri.toString(),
                        name = newDoc.name ?: fileName,
                        uri = newDoc.uri,
                        isDirectory = false,
                        fileType = FileType.fromFileName(newDoc.name ?: fileName)
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Creates a new subfolder in the specified directory DocumentFile.
     */
    suspend fun createDirectory(parentUri: Uri, dirName: String): Result<FileItem> =
        withContext(Dispatchers.IO) {
            try {
                val parentDoc = DocumentFile.fromTreeUri(context, parentUri)
                    ?: DocumentFile.fromSingleUri(context, parentUri)
                    ?: return@withContext Result.failure(Exception("Parent directory not found"))

                val newDir = parentDoc.createDirectory(dirName)
                    ?: return@withContext Result.failure(Exception("Failed to create directory $dirName"))

                Result.success(
                    FileItem(
                        id = newDir.uri.toString(),
                        name = newDir.name ?: dirName,
                        uri = newDir.uri,
                        isDirectory = true
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Deletes a file or directory DocumentFile.
     */
    suspend fun delete(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val doc = DocumentFile.fromSingleUri(context, uri)
                ?: DocumentFile.fromTreeUri(context, uri)
            val success = doc?.delete() == true
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Renames a file or directory DocumentFile.
     */
    suspend fun rename(uri: Uri, newName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val doc = DocumentFile.fromSingleUri(context, uri)
                ?: DocumentFile.fromTreeUri(context, uri)
            val success = doc?.renameTo(newName) == true
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads the hierarchical file tree from a Tree Uri picked via SAF.
     */
    suspend fun loadTree(treeUri: Uri): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(Exception("Invalid root tree URI"))

            val rootItem = buildTreeRecursive(rootDoc)
            Result.success(rootItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildTreeRecursive(doc: DocumentFile): FileItem {
        val isDir = doc.isDirectory
        val children = if (isDir) {
            doc.listFiles()
                .map { buildTreeRecursive(it) }
                .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } else {
            emptyList()
        }

        return FileItem(
            id = doc.uri.toString(),
            name = doc.name ?: (if (isDir) "Folder" else "File"),
            uri = doc.uri,
            isDirectory = isDir,
            children = children,
            fileType = if (isDir) FileType.UNKNOWN else FileType.fromFileName(doc.name ?: ""),
            isExpanded = true,
            sizeBytes = if (isDir) 0 else doc.length()
        )
    }

    /**
     * Generates a rich demo workspace with Kotlin, Python, HTML/JS, and Markdown files
     * for instant testing without requiring file picking first.
     */
    fun createSampleWorkspace(): Pair<FileItem, Map<String, String>> {
        val sampleContents = mapOf(
            "sample_kotlin_main" to """
                package com.antigravity.demo

                /**
                 * Antigravity IDE - Native Android AI Code Editor
                 * AI Agent can directly modify this buffer!
                 */
                fun main() {
                    val ideName = "Antigravity IDE"
                    val version = "0.0.3"
                    
                    println("🚀 Welcome to ${'$'}ideName v${'$'}version!")
                    println("🤖 Powered by Google Gemini AI SDK for Android")
                    
                    val features = listOf(
                        "Direct Code Manipulation without Copy-Paste",
                        "Storage Access Framework (Local Device Files)",
                        "Sora Native Android Code Editor",
                        "Live HTML / Web Previewer"
                    )
                    
                    println("\nCore Features:")
                    features.forEachIndexed { index, feature ->
                        println("  ${'$'}{index + 1}. ${'$'}feature")
                    }
                }
            """.trimIndent(),

            "sample_python_calc" to """
                # Antigravity IDE - Python Sample
                import math

                class AntigravityEngine:
                    def __init__(self, name="Antigravity"):
                        self.name = name
                        self.gravity_offset = 9.80665

                    def calculate_escape_velocity(self, mass_kg: float, radius_m: float) -> float:
                        # Computes escape velocity from planetary mass and radius.
                        G = 6.67430e-11
                        return math.sqrt(2 * G * mass_kg / radius_m)

                    def optimize_trajectory(self, waypoints: list) -> dict:
                        print(f"[{self.name}] Optimizing {len(waypoints)} orbital waypoints...")
                        return {"status": "OPTIMAL", "fuel_efficiency": 0.94}

                if __name__ == "__main__":
                    engine = AntigravityEngine()
                    # Earth: mass ~ 5.972e24 kg, radius ~ 6.371e6 m
                    v_esc = engine.calculate_escape_velocity(5.972e24, 6.371e6)
                    print(f"Earth Escape Velocity: {v_esc:.2f} m/s ({v_esc / 1000:.2f} km/s)")
            """.trimIndent(),

            "sample_web_index" to """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Antigravity IDE - Live Web Preview</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 24px;
                            background: #0f1117;
                            color: #e2e8f0;
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            min-height: 85vh;
                            text-align: center;
                        }
                        .badge {
                            background: linear-gradient(135deg, #6c63ff, #00e5ff);
                            color: white;
                            padding: 6px 14px;
                            border-radius: 20px;
                            font-size: 13px;
                            font-weight: bold;
                            margin-bottom: 16px;
                            letter-spacing: 0.5px;
                        }
                        h1 {
                            font-size: 32px;
                            margin: 0 0 12px 0;
                            background: linear-gradient(135deg, #ffffff, #a5b4fc);
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                        }
                        p {
                            color: #94a3b8;
                            max-width: 480px;
                            line-height: 1.6;
                            font-size: 16px;
                        }
                        .card {
                            background: #1a1d27;
                            border: 1px solid #2e3547;
                            border-radius: 12px;
                            padding: 20px;
                            margin-top: 24px;
                            width: 100%;
                            max-width: 420px;
                            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
                        }
                        button {
                            background: #6c63ff;
                            color: white;
                            border: none;
                            padding: 12px 24px;
                            font-size: 15px;
                            font-weight: 600;
                            border-radius: 8px;
                            cursor: pointer;
                            transition: all 0.2s;
                        }
                        button:active {
                            transform: scale(0.97);
                            background: #5348e0;
                        }
                    </style>
                </head>
                <body>
                    <div class="badge">ANTIGRAVITY IDE FOR ANDROID</div>
                    <h1>Live Code Execution</h1>
                    <p>Edit this HTML/CSS/JS file and tap the <strong>Run / Preview</strong> button in the top bar to see live updates!</p>
                    <div class="card">
                        <button onclick="triggerGreeting()">Click to Interact</button>
                        <p id="output" style="margin-top: 14px; color: #00e5ff; font-weight: 500;">Ready</p>
                    </div>
                    <script>
                        let clicks = 0;
                        function triggerGreeting() {
                            clicks++;
                            document.getElementById('output').textContent = 
                                '⚡ Action triggered #' + clicks + ' - Generated on device!';
                        }
                    </script>
                </body>
                </html>
            """.trimIndent(),

            "sample_readme" to """
                # 🪐 Antigravity IDE for Android

                A native, AI-powered desktop-grade code editor for Android built with Kotlin & Jetpack Compose.

                ## ✨ Key Capabilities
                - **Direct AI Code Injection**: Never copy-paste code! The AI agent modifies your editor directly.
                - **Storage Access Framework (SAF)**: Open real device folders and external storage seamlessly.
                - **Sora Code Editor**: Syntax highlighting, line numbers, and fast programmatic text editing.
                - **Built-in Web Runner**: Preview HTML, CSS, and JavaScript with interactive WebView.
                - **Google Gemini SDK**: Powered by `gemini-1.5-flash` or `gemini-1.5-pro`.

                ## 🛠 How to Use
                1. Tap any file in the Explorer on the left.
                2. Tap the **AI Assistant** icon to open the chat panel.
                3. Ask Gemini to write a function or fix a bug.
                4. Tap **Apply to Editor** — watch your code update automatically!
            """.trimIndent()
        )

        val root = FileItem(
            id = "demo_project_root",
            name = "Antigravity Demo Project",
            uri = null,
            isDirectory = true,
            isExpanded = true,
            children = listOf(
                FileItem(
                    id = "sample_kotlin_main",
                    name = "Main.kt",
                    uri = null,
                    isDirectory = false,
                    fileType = FileType.KOTLIN
                ),
                FileItem(
                    id = "sample_python_calc",
                    name = "calculator.py",
                    uri = null,
                    isDirectory = false,
                    fileType = FileType.PYTHON
                ),
                FileItem(
                    id = "sample_web_index",
                    name = "index.html",
                    uri = null,
                    isDirectory = false,
                    fileType = FileType.HTML
                ),
                FileItem(
                    id = "sample_readme",
                    name = "README.md",
                    uri = null,
                    isDirectory = false,
                    fileType = FileType.MARKDOWN
                )
            )
        )

        return Pair(root, sampleContents)
    }
}
