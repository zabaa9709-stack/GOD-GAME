package com.gamegesture.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gamegesture.app.ui.calibration.CalibrationWizardScreen
import com.gamegesture.app.ui.creator.GestureCreatorScreen
import com.gamegesture.app.ui.debug.DebugScreen
import com.gamegesture.app.ui.home.HomeScreen
import com.gamegesture.app.ui.library.GestureLibraryScreen
import com.gamegesture.app.ui.practice.PracticeModeScreen
import com.gamegesture.app.ui.recorder.GestureRecorderScreen
import com.gamegesture.app.ui.settings.SettingsScreen
import com.gamegesture.app.ui.shortcut.ShortcutConfigScreen

@Composable
fun GameGestureNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = Screen.Creator.route,
            arguments = listOf(
                navArgument("gestureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val gestureId = backStackEntry.arguments?.getString("gestureId")
            GestureCreatorScreen(
                gestureId = gestureId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Recorder.route) {
            GestureRecorderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Practice.route,
            arguments = listOf(
                navArgument("gestureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val gestureId = backStackEntry.arguments?.getString("gestureId")
            PracticeModeScreen(
                gestureId = gestureId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.Creator.createRoute(id)) }
            )
        }

        composable(Screen.Calibration.route) {
            CalibrationWizardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Library.route) {
            GestureLibraryScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.Creator.createRoute(id)) },
                onPractice = { id -> navController.navigate(Screen.Practice.createRoute(id)) },
                onCreateNew = { navController.navigate(Screen.Creator.createRoute()) }
            )
        }

        composable(Screen.ShortcutConfig.route) {
            ShortcutConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
