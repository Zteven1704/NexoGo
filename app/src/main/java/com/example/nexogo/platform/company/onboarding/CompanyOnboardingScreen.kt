package com.example.nexogo.platform.company.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.PersistentAuthViewModel

/**
 * Host screen for CreateCompanyFlow (wizard + admin setup).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyOnboardingScreen(
    onCompleted: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()

    val flow = remember(currentUser?.id) {
        CreateCompanyFlow.create(
            context = context,
            userId = currentUser?.id.orEmpty(),
            email = currentUser?.email.orEmpty(),
            displayName = currentUser?.name.orEmpty()
        )
    }
    val ui by flow.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ui.completed) {
        if (ui.completed) onCompleted()
    }

    val progress = when (ui.step) {
        OnboardingStep.COMPANY_SETUP -> 0.33f
        OnboardingStep.PLAN_SELECTION -> 0.66f
        OnboardingStep.ADMIN_SETUP, OnboardingStep.COMPLETING -> 1f
        OnboardingStep.DONE -> 1f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar empresa") },
                actions = {
                    IconButton(onClick = {
                        authVm.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            when (ui.step) {
                OnboardingStep.COMPANY_SETUP, OnboardingStep.PLAN_SELECTION -> {
                    CompanySetupWizard(
                        draft = ui.draft,
                        step = ui.step,
                        onDraftChange = { flow.setDraft(it) },
                        onContinueFromCompany = { flow.goToPlan() },
                        onContinueFromPlan = { flow.goToAdmin() },
                        onBack = { flow.back() }
                    )
                }

                OnboardingStep.ADMIN_SETUP, OnboardingStep.COMPLETING -> {
                    InitialAdminSetup(
                        draft = ui.draft,
                        authEmail = currentUser?.email.orEmpty(),
                        isSubmitting = ui.isSubmitting,
                        onDraftChange = { flow.setDraft(it) },
                        onSubmit = { flow.submit() },
                        onBack = { flow.back() }
                    )
                }

                OnboardingStep.DONE -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Entrando al sistema…")
                        }
                    }
                }
            }

            ui.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(err, color = MaterialTheme.colorScheme.error)
            }

            if (currentUser == null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "No hay sesión. Inicia sesión o regístrate para continuar.",
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = onLogout) { Text("Ir a login") }
            }
        }
    }
}
