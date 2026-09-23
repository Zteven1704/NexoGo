package com.example.nexogo.platform.role.nav

import com.example.nexogo.core.models.UserRole
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.role.compat.LegacyRoleBridge
import com.example.nexogo.platform.role.engine.PermissionAction
import com.example.nexogo.platform.role.engine.PermissionEngine
import com.example.nexogo.platform.role.engine.PermissionModule
import com.example.nexogo.platform.role.model.BaseRoleCodes

/**
 * Builds [PermissionEngine] for hub navigation from membership + legacy role bridge.
 * Does not alter legacy modules — only gates Home drawer / quick actions.
 */
object NavPermissionFactory {

    fun forHub(legacyRole: UserRole?, membership: CompanyMembership?): PermissionEngine {
        val fromMembership = membership?.roleCodes.orEmpty().filter { it.isNotBlank() }
        val codes = if (fromMembership.isNotEmpty()) {
            fromMembership
        } else {
            listOf(LegacyRoleBridge.toBaseRoleCode(legacyRole ?: UserRole.USER))
        }
        return PermissionEngine.fromRoleCodes(codes)
    }

    fun showClients(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.CLIENTS)

    fun showRecords(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.CLIENTS) || engine.canEnter(PermissionModule.DOCUMENTS)

    fun showDocuments(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.DOCUMENTS)

    /** Users / Roles / Permissions hub entries (ADMIN+). */
    fun showAdmin(engine: PermissionEngine): Boolean =
        engine.hasRole(BaseRoleCodes.ADMIN) || engine.isSuperAdmin()

    fun showUsers(engine: PermissionEngine): Boolean = showAdmin(engine)

    fun showRoles(engine: PermissionEngine): Boolean = showAdmin(engine)

    fun showPermissions(engine: PermissionEngine): Boolean = showAdmin(engine)

    // Legacy gates kept for routes still in NavGraph (not linked from Home hub).
    fun showPatients(engine: PermissionEngine): Boolean = showClients(engine)

    fun showAppointments(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.CLIENTS) || engine.canEnter(PermissionModule.CHAT)

    fun showClinical(engine: PermissionEngine): Boolean = showRecords(engine)

    fun showInventory(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.INVENTORY)

    fun showSales(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.SALES)

    fun showChat(engine: PermissionEngine): Boolean =
        engine.canEnter(PermissionModule.CHAT)

    fun showReports(engine: PermissionEngine): Boolean =
        engine.can(PermissionModule.SALES, PermissionAction.EXPORT)
            || engine.canEnter(PermissionModule.SALES)
}
