package com.antigravity.ide.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IdeSettings(
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-1.5-flash",
    val fontSizeSp: Float = 14f,
    val wordWrap: Boolean = false,
    val tabSize: Int = 4,
    val autoSave: Boolean = true
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("antigravity_ide_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<IdeSettings> = _settings.asStateFlow()

    private fun loadSettings(): IdeSettings {
        return IdeSettings(
            geminiApiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            geminiModel = prefs.getString(KEY_MODEL, "gemini-1.5-flash") ?: "gemini-1.5-flash",
            fontSizeSp = prefs.getFloat(KEY_FONT_SIZE, 14f),
            wordWrap = prefs.getBoolean(KEY_WORD_WRAP, false),
            tabSize = prefs.getInt(KEY_TAB_SIZE, 4),
            autoSave = prefs.getBoolean(KEY_AUTO_SAVE, true)
        )
    }

    fun updateApiKey(newKey: String) {
        prefs.edit().putString(KEY_API_KEY, newKey.trim()).apply()
        _settings.value = _settings.value.copy(geminiApiKey = newKey.trim())
    }

    fun updateModel(newModel: String) {
        prefs.edit().putString(KEY_MODEL, newModel).apply()
        _settings.value = _settings.value.copy(geminiModel = newModel)
    }

    fun updateFontSize(newSize: Float) {
        val clamped = newSize.coerceIn(10f, 32f)
        prefs.edit().putFloat(KEY_FONT_SIZE, clamped).apply()
        _settings.value = _settings.value.copy(fontSizeSp = clamped)
    }

    fun updateWordWrap(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WORD_WRAP, enabled).apply()
        _settings.value = _settings.value.copy(wordWrap = enabled)
    }

    fun updateTabSize(size: Int) {
        val clamped = size.coerceIn(2, 8)
        prefs.edit().putInt(KEY_TAB_SIZE, clamped).apply()
        _settings.value = _settings.value.copy(tabSize = clamped)
    }

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "gemini_model"
        private const val KEY_FONT_SIZE = "font_size_sp"
        private const val KEY_WORD_WRAP = "word_wrap"
        private const val KEY_TAB_SIZE = "tab_size"
        private const val KEY_AUTO_SAVE = "auto_save"
    }
}
