package com.example.nexogo.platform.company.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nexogo.platform.company.model.CompanyRoleCodes

/**
 * Step 3: confirm initial ADMIN user (the signed-in account becomes company admin).
 */
@Composable
fun InitialAdminSetup(
    draft: OnboardingDraft,
    authEmail: String,
    isSubmitting: Boolean,
    onDraftChange: (OnboardingDraft) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Administrador inicial", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Tu cuenta será el administrador (rol ${CompanyRoleCodes.ADMIN}) de la empresa. " +
                "Podrás invitar al equipo más adelante.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = draft.adminDisplayName,
            onValueChange = { onDraftChange(draft.copy(adminDisplayName = it)) },
            label = { Text("Nombre del administrador *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )
        OutlinedTextField(
            value = draft.adminTitle,
            onValueChange = { onDraftChange(draft.copy(adminTitle = it)) },
            label = { Text("Cargo / título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )
        OutlinedTextField(
            value = draft.primaryContactEmail.ifBlank { authEmail },
            onValueChange = { onDraftChange(draft.copy(primaryContactEmail = it)) },
            label = { Text("Email de contacto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting
        )
        Text(
            "Cuenta Auth: ${authEmail.ifBlank { "(sesión actual)" }}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Empresa: ${draft.companyName} · Plan: ${draft.planId}",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = draft.canProceedAdmin && !isSubmitting
        ) {
            Text(if (isSubmitting) "Creando empresa…" else "Crear empresa y entrar")
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) { Text("Atrás") }
    }
}
