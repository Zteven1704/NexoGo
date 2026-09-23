package com.example.nexogo.platform.company.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Steps 1–2 of onboarding: company identity + initial plan.
 */
@Composable
fun CompanySetupWizard(
    draft: OnboardingDraft,
    step: OnboardingStep,
    onDraftChange: (OnboardingDraft) -> Unit,
    onContinueFromCompany: () -> Unit,
    onContinueFromPlan: () -> Unit,
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
        when (step) {
            OnboardingStep.COMPANY_SETUP -> {
                Text("Tu empresa", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Define el nombre y el tipo de negocio. Podrás cambiarlos después.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = draft.companyName,
                    onValueChange = { onDraftChange(draft.copy(companyName = it)) },
                    label = { Text("Nombre comercial *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.legalName,
                    onValueChange = { onDraftChange(draft.copy(legalName = it)) },
                    label = { Text("Razón social (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Industria", style = MaterialTheme.typography.titleSmall)
                OnboardingIndustryOptions.all.forEach { (id, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = draft.industryPack == id,
                                onClick = { onDraftChange(draft.copy(industryPack = id)) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = draft.industryPack == id,
                            onClick = { onDraftChange(draft.copy(industryPack = id)) }
                        )
                        Text(label, Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onContinueFromCompany,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = draft.canProceedCompany
                ) { Text("Continuar") }
            }

            OnboardingStep.PLAN_SELECTION -> {
                Text("Plan inicial", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Elige el plan de partida. La facturación real puede activarse después.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OnboardingPlanCatalog.options.forEach { plan ->
                    val selected = draft.planId == plan.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDraftChange(draft.copy(planId = plan.id)) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { onDraftChange(draft.copy(planId = plan.id)) }
                                )
                                Column(Modifier.padding(start = 4.dp)) {
                                    Text(
                                        plan.title + if (plan.recommended) " · recomendado" else "",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        plan.subtitle,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onContinueFromPlan,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Atrás") }
            }

            else -> Unit
        }
    }
}
