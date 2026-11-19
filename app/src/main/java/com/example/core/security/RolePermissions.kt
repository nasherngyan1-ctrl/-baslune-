package com.example.core.security

import com.example.domain.model.AppModule
import com.example.domain.model.UserRole

enum class BusinessPermission {
    VIEW_DASHBOARD,
    VIEW_CLIENTS,
    EDIT_CLIENTS,
    ARCHIVE_CLIENTS,
    VIEW_PROJECTS,
    EDIT_PROJECTS,
    ARCHIVE_PROJECTS,
    VIEW_TASKS,
    EDIT_TASKS,
    VIEW_SERVICES,
    EDIT_SERVICES,
    VIEW_SALES,
    EDIT_SALES,
    VIEW_FINANCE,
    EDIT_FINANCE,
    DELETE_FINANCE,
    VIEW_INVOICES,
    EDIT_INVOICES,
    RECORD_PAYMENTS,
    VIEW_MARKETING,
    EDIT_MARKETING,
    VIEW_DOCUMENTS,
    EDIT_DOCUMENTS,
    VIEW_KNOWLEDGE,
    EDIT_KNOWLEDGE,
    VIEW_REPORTS,
    VIEW_FINANCIAL_REPORTS,
    MANAGE_TEAM,
    MANAGE_SETTINGS,
    BACKUP_RESTORE
}

object RolePermissions {
    fun permissionsFor(role: UserRole): Set<BusinessPermission> = when (role) {
        UserRole.OWNER -> BusinessPermission.entries.toSet()
        UserRole.ADMINISTRATOR -> setOf(
            BusinessPermission.VIEW_DASHBOARD,
            BusinessPermission.VIEW_CLIENTS,
            BusinessPermission.EDIT_CLIENTS,
            BusinessPermission.ARCHIVE_CLIENTS,
            BusinessPermission.VIEW_PROJECTS,
            BusinessPermission.EDIT_PROJECTS,
            BusinessPermission.ARCHIVE_PROJECTS,
            BusinessPermission.VIEW_TASKS,
            BusinessPermission.EDIT_TASKS,
            BusinessPermission.VIEW_SERVICES,
            BusinessPermission.EDIT_SERVICES,
            BusinessPermission.VIEW_SALES,
            BusinessPermission.EDIT_SALES,
            BusinessPermission.VIEW_MARKETING,
            BusinessPermission.EDIT_MARKETING,
            BusinessPermission.VIEW_DOCUMENTS,
            BusinessPermission.EDIT_DOCUMENTS,
            BusinessPermission.VIEW_KNOWLEDGE,
            BusinessPermission.EDIT_KNOWLEDGE,
            BusinessPermission.VIEW_REPORTS
        )
        UserRole.STAFF -> setOf(
            BusinessPermission.VIEW_DASHBOARD,
            BusinessPermission.VIEW_PROJECTS,
            BusinessPermission.EDIT_PROJECTS,
            BusinessPermission.VIEW_TASKS,
            BusinessPermission.EDIT_TASKS,
            BusinessPermission.VIEW_DOCUMENTS,
            BusinessPermission.VIEW_KNOWLEDGE
        )
        UserRole.FINANCE -> setOf(
            BusinessPermission.VIEW_DASHBOARD,
            BusinessPermission.VIEW_CLIENTS,
            BusinessPermission.VIEW_PROJECTS,
            BusinessPermission.VIEW_FINANCE,
            BusinessPermission.EDIT_FINANCE,
            BusinessPermission.DELETE_FINANCE,
            BusinessPermission.VIEW_INVOICES,
            BusinessPermission.EDIT_INVOICES,
            BusinessPermission.RECORD_PAYMENTS,
            BusinessPermission.VIEW_REPORTS,
            BusinessPermission.VIEW_FINANCIAL_REPORTS
        )
    }

    fun hasPermission(role: UserRole, permission: BusinessPermission): Boolean =
        permissionsFor(role).contains(permission)

    fun canAccessModule(role: UserRole, module: AppModule): Boolean = when (module) {
        AppModule.DASHBOARD -> hasPermission(role, BusinessPermission.VIEW_DASHBOARD)
        AppModule.CLIENTS -> hasPermission(role, BusinessPermission.VIEW_CLIENTS)
        AppModule.PROJECTS -> hasPermission(role, BusinessPermission.VIEW_PROJECTS)
        AppModule.TASKS -> hasPermission(role, BusinessPermission.VIEW_TASKS)
        AppModule.SERVICES -> hasPermission(role, BusinessPermission.VIEW_SERVICES)
        AppModule.SALES -> hasPermission(role, BusinessPermission.VIEW_SALES)
        AppModule.FINANCE -> hasPermission(role, BusinessPermission.VIEW_FINANCE)
        AppModule.INVOICES -> hasPermission(role, BusinessPermission.VIEW_INVOICES)
        AppModule.MARKETING -> hasPermission(role, BusinessPermission.VIEW_MARKETING)
        AppModule.DOCUMENTS -> hasPermission(role, BusinessPermission.VIEW_DOCUMENTS)
        AppModule.KNOWLEDGE -> hasPermission(role, BusinessPermission.VIEW_KNOWLEDGE)
        AppModule.REPORTS -> hasPermission(role, BusinessPermission.VIEW_REPORTS)
        AppModule.TEAM -> hasPermission(role, BusinessPermission.MANAGE_TEAM)
        AppModule.SETTINGS -> hasPermission(role, BusinessPermission.MANAGE_SETTINGS)
        AppModule.BACKUP -> hasPermission(role, BusinessPermission.BACKUP_RESTORE)
    }

    fun requirePermission(role: UserRole, permission: BusinessPermission) {
        if (!hasPermission(role, permission)) {
            throw SecurityException("Access denied for role ${role.name}: missing ${permission.name}")
        }
    }
}
