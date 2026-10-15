package com.gamegesture.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Creator : Screen("creator?gestureId={gestureId}") {
        fun createRoute(gestureId: String? = null): String {
            return if (gestureId != null) "creator?gestureId=$gestureId" else "creator"
        }
    }
    object Recorder : Screen("recorder")
    object Library : Screen("library")
    object Practice : Screen("practice?gestureId={gestureId}") {
        fun createRoute(gestureId: String? = null): String {
            return if (gestureId != null) "practice?gestureId=$gestureId" else "practice"
        }
    }
    object Calibration : Screen("calibration")
    object ShortcutConfig : Screen("shortcut_config")
    object Profiles : Screen("profiles")
    object Settings : Screen("settings")
    object Debug : Screen("debug")
}
