package com.example.nexogo.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * Único splash de entrada: sesión Firebase → Home o Login.
 */
@Composable
fun SessionSplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NexoGo", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
        }
    }

    LaunchedEffect(Unit) {
        delay(400)
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            onNavigateToLogin()
            return@LaunchedEffect
        }
        val authVm = PersistentAuthViewModel.getInstance(context)
        authVm.refreshUserFromDataStore()
        delay(150)
        onNavigateToHome()
    }
}
