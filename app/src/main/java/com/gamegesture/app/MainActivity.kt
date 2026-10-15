package com.gamegesture.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gamegesture.app.ui.navigation.GameGestureNavGraph
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.GameGestureTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameGestureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberDarkBg
                ) {
                    val navController = rememberNavController()
                    GameGestureNavGraph(navController = navController)
                }
            }
        }
    }
}
