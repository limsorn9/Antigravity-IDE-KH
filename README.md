# 🪐 Antigravity IDE for Android

**Antigravity IDE** is a native, AI-powered desktop-grade code editor for Android built entirely with **Kotlin** and **Jetpack Compose**. It is engineered to give developers a full-featured, mobile IDE workspace where the integrated **Google Gemini AI** can inspect and directly modify editor code buffers without tedious copy-pasting.

---

## 🚀 Core Features

1. **Desktop IDE Experience on Mobile**:
   - **Collapsible File Explorer**: Browse project hierarchies, open folders, create new files, and inspect icons customized for Kotlin, Java, Python, JavaScript, HTML, CSS, JSON, and Markdown.
   - **Multi-Tab Workspace**: Open multiple files simultaneously, switch tabs with ease, track unsaved changes with dirty indicators (`*`), and save files on demand.
   - **Full Command Bar**: Undo, Redo, Save, Quick AI Fix (`AI Fix`), Live Preview, and Settings.
   - **Live Status Bar**: Real-time cursor coordinates (`Ln X, Col Y`), selection character count, encoding, and active language syntax.

2. **Native Sora Code Editor Integration**:
   - High-performance native code editor engine powered by `io.github.rosemoe.sora-editor:editor`.
   - Dark cyber theme, customizable font sizes, word wrap, tab sizing, and line numbers.
   - Programmatic `EditorController` supporting direct text replacement, cursor insertion, and range substitution.

3. **Seamless AI Integration (Google Generative AI SDK / Gemini)**:
   - Powered by official Google Generative AI SDK (`com.google.ai.client.generativeai:generativeai`).
   - **Direct Code Injection**: Never copy-paste! Tap **"Apply to Editor"**, **"Insert at Cursor"**, or **"Replace Selection"** on any generated code block to mutate the open file immediately.
   - **Context-Aware Prompting**: Automatically packages current file content, active language, and selected code snippet with every request.
   - **⚡ Quick AI Fix**: 1-tap automated bug detection and code optimization.

4. **Local File Management & Storage Access Framework (SAF)**:
   - Full integration with Android's Storage Access Framework (`OpenDocumentTree`, `DocumentFile`).
   - Persistable permissions across device reboots.
   - Built-in rich **Demo Project** (Kotlin, Python, HTML/JS, and Markdown) for immediate testing without needing to select a device folder first.

5. **Live Web & HTML Runner**:
   - Built-in interactive Web runner (`PreviewDialog`) using Android `WebView` to test and execute HTML, CSS, and JavaScript applications directly on your phone or tablet.

---

## 📂 Project Architecture

```
app/src/main/java/com/antigravity/ide/
├── MainActivity.kt                       # Entry activity, SAF folder picker launcher
├── data/
│   ├── ai/
│   │   └── GeminiService.kt              # Google Generative AI SDK, prompt engine, code extraction
│   ├── model/
│   │   ├── AiMessage.kt                  # Chat messages, code blocks, apply modes
│   │   └── FileModel.kt                  # FileItem, FileType, EditorTab models
│   ├── preferences/
│   │   └── SettingsManager.kt            # Gemini API Key, model selector, editor preferences
│   └── repository/
│       └── FileManager.kt                # SAF DocumentFile operations, recursive tree, demo workspace
└── ui/
    ├── components/
    │   ├── AiAssistantPanel.kt           # AI chat panel, prompt chips, direct code action buttons
    │   ├── CodeEditorView.kt             # Sora CodeEditor wrapper with Compose AndroidView
    │   ├── FileExplorerPanel.kt          # Collapsible tree view, file creation, folder opener
    │   ├── IdeTabBar.kt                  # Tab management with close and modified indicators
    │   ├── IdeTopBar.kt                  # Action buttons: Save, Preview, Quick Fix, Undo, Redo
    │   ├── PreviewDialog.kt              # Live WebView runner for HTML/JS
    │   └── SettingsDialog.kt             # Gemini API key and model configuration dialog
    ├── screens/
    │   └── IdeMainScreen.kt              # Central responsive IDE layout
    └── theme/
        ├── Color.kt                      # Cyber/Studio dark palette
        ├── Theme.kt                      # Material3 dark color scheme
        └── Type.kt                       # Monospace and sans-serif typography
```

---

## 🛠 Prerequisites & Building

- **Android Studio** (Koala / Ladybug or newer recommended)
- **JDK**: Java 17+
- **Android SDK**: Compile SDK 35, Min SDK 26 (Android 8.0+)
- **Free Google Gemini API Key**: Obtain one from [Google AI Studio](https://aistudio.google.com).

### Opening in Android Studio
1. Open Android Studio.
2. Select **File > Open...** and navigate to this folder: `d:\All Tool Project\KH AntigravityIDE`.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator or physical device (`Shift + F10`).
5. Open Settings (gear icon in the top right), enter your Gemini API Key, and start coding!
