package com.example.nexogo.platform.company.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.nexogo.platform.company.model.Company
import com.example.nexogo.platform.company.model.CompanyModel
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.company.session.CompanySessionState

/**
 * Active company for Compose tree. Null when no session bound yet.
 */
val LocalActiveCompany = compositionLocalOf<CompanyModel?> { null }

val LocalCompanySession = compositionLocalOf<CompanySessionState> {
    CompanySessionState()
}

val LocalCompanySessionManager = compositionLocalOf<CompanySessionManager?> { null }

/**
 * Provides [LocalActiveCompany] / [LocalCompanySession] from [CompanySessionManager].
 * Additive wrapper — does not alter navigation destinations.
 */
@Composable
fun CompanyProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { CompanySessionManager.getInstance(context) }
    val session by manager.session.collectAsState()

    CompositionLocalProvider(
        LocalCompanySessionManager provides manager,
        LocalCompanySession provides session,
        LocalActiveCompany provides session.company
    ) {
        content()
    }
}

/** Convenience read of active company id in Compose. */
@Composable
fun rememberActiveCompanyId(): String? {
    val company = LocalActiveCompany.current
    return company?.id
}

@Composable
fun rememberActiveCompany(): Company? = LocalActiveCompany.current
