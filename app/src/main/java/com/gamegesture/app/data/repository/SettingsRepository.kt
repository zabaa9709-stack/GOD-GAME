package com.gamegesture.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.gamegesture.app.data.model.AppSettings
import com.gamegesture.app.data.model.ShortcutConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_gesture_settings", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _shortcutConfig = MutableStateFlow(loadShortcutConfig())
    val shortcutConfig: StateFlow<ShortcutConfig> = _shortcutConfig.asStateFlow()

    private fun loadSettings(): AppSettings {
        val raw = prefs.getString("key_app_settings", null) ?: return AppSettings()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun loadShortcutConfig(): ShortcutConfig {
        val raw = prefs.getString("key_shortcut_config", null)
            ?: return ShortcutConfig(gestureSequenceId = "preset_360_movement")
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            ShortcutConfig(gestureSequenceId = "preset_360_movement")
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().putString("key_app_settings", json.encodeToString(newSettings)).apply()
    }

    fun updateShortcutConfig(newConfig: ShortcutConfig) {
        _shortcutConfig.value = newConfig
        prefs.edit().putString("key_shortcut_config", json.encodeToString(newConfig)).apply()
    }
}
