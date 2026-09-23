package com.example.nexogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nexogo.core.firebase.FirebaseErrorHandler
import com.example.nexogo.navigation.NavGraph
import com.example.nexogo.navigation.Screen
import com.example.nexogo.platform.company.ui.CompanyProvider
import com.example.nexogo.ui.theme.NexoGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            FirebaseErrorHandler.handleError(Exception(throwable), "GlobalException")
        }

        setContent {
            NexoGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompanyProvider {
                        NexoGoApp()
                    }
                }
            }
        }
    }
}

@Composable
fun NexoGoApp() {
    val navController = rememberNavController()
    NavGraph(
        navController = navController,
        startDestination = Screen.Splash.route
    )
}
