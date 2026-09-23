package com.example.nexogo.platform.company.onboarding

import com.example.nexogo.platform.company.model.CompanyLimits
import com.example.nexogo.platform.company.model.CompanyPlans
import com.example.nexogo.platform.company.model.IndustryPacks

/**
 * Steps for [CreateCompanyFlow] — register → company → plan → admin → enter hub.
 */
enum class OnboardingStep {
    COMPANY_SETUP,
    PLAN_SELECTION,
    ADMIN_SETUP,
    COMPLETING,
    DONE
}

/**
 * Draft collected across [CompanySetupWizard] + [InitialAdminSetup].
 */
data class OnboardingDraft(
    val companyName: String = "",
    val legalName: String = "",
    val industryPack: String = IndustryPacks.VETERINARY,
    val planId: String = CompanyPlans.FREE,
    val adminDisplayName: String = "",
    val adminTitle: String = "Administrador",
    val primaryContactEmail: String = ""
) {
    val canProceedCompany: Boolean
        get() = companyName.trim().length >= 2

    val canProceedAdmin: Boolean
        get() = adminDisplayName.trim().length >= 2
}

/**
 * Catalog of initial plans shown in the wizard (billing later).
 */
data class PlanOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val limits: CompanyLimits,
    val recommended: Boolean = false
)

object OnboardingPlanCatalog {
    val options: List<PlanOption> = listOf(
        PlanOption(
            id = CompanyPlans.FREE,
            title = "Free",
            subtitle = "Ideal para empezar · 5 usuarios · 100 clientes",
            limits = CompanyLimits(
                maxUsers = 5,
                maxStorageMb = 512,
                maxAiRequestsMonth = 50,
                maxClients = 100,
                maxCustomRoles = 3
            )
        ),
        PlanOption(
            id = CompanyPlans.PRO,
            title = "Pro",
            subtitle = "Equipos en crecimiento · 20 usuarios · 1 000 clientes",
            limits = CompanyLimits(
                maxUsers = 20,
                maxStorageMb = 5_120,
                maxAiRequestsMonth = 500,
                maxClients = 1_000,
                maxCustomRoles = 10
            ),
            recommended = true
        ),
        PlanOption(
            id = CompanyPlans.BUSINESS,
            title = "Business",
            subtitle = "Operación multi-área · 50 usuarios · 5 000 clientes",
            limits = CompanyLimits(
                maxUsers = 50,
                maxStorageMb = 20_480,
                maxAiRequestsMonth = 2_000,
                maxClients = 5_000,
                maxCustomRoles = 25
            )
        ),
        PlanOption(
            id = CompanyPlans.ENTERPRISE,
            title = "Enterprise",
            subtitle = "Límites altos · soporte prioritario",
            limits = CompanyLimits(
                maxUsers = 200,
                maxStorageMb = 102_400,
                maxAiRequestsMonth = 10_000,
                maxClients = 50_000,
                maxCustomRoles = 100
            )
        )
    )

    fun limitsFor(planId: String): CompanyLimits =
        options.find { it.id == planId }?.limits ?: options.first().limits
}

object OnboardingIndustryOptions {
    val all: List<Pair<String, String>> = listOf(
        IndustryPacks.VETERINARY to "Veterinaria",
        IndustryPacks.CLINIC to "Clínica",
        IndustryPacks.WAREHOUSE to "Bodega / inventario",
        IndustryPacks.BUSINESS_B2B to "Negocio B2B",
        IndustryPacks.WORKSHOP to "Taller",
        IndustryPacks.CONSULTING to "Consultoría"
    )
}
