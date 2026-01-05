package com.example.nexogo.modules.domicilios.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.viewmodel.PersistentAuthViewModel

/**
 * Pantalla principal de Domicilios que redirige según el rol del usuario
 */
@Composable
fun DomicilioMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdminDetail: (String) -> Unit = onNavigateToDetail,
    onNavigateToVetDetail: (String) -> Unit = onNavigateToDetail
) {
    val viewModel = PersistentAuthViewModel.getInstance(LocalContext.current)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    when (currentUser?.role) {
        UserRole.ADMIN -> {
            AdminDomicilioListScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToDetail = onNavigateToAdminDetail,
                onNavigateToCreate = onNavigateToCreate
            )
        }
        UserRole.VET, UserRole.VET_ASSISTANT -> {
            VetDomicilioListScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToDetail = onNavigateToVetDetail,
                onNavigateToCreate = onNavigateToCreate
            )
        }
        UserRole.USER, null -> {
            DomicilioListScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

