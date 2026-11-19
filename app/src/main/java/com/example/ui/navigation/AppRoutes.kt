package com.example.ui.navigation

object AppRoutes {
    const val DASHBOARD = "dashboard"
    const val CLIENTS = "clients"
    const val CLIENT_DETAIL = "client_detail/{clientId}"
    const val PROJECTS = "projects"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    const val TASKS = "tasks"
    const val SERVICES = "services"
    const val SALES = "sales"
    const val FINANCE = "finance"
    const val INVOICES = "invoices"
    const val INVOICE_DETAIL = "invoice_detail/{invoiceId}"
    const val MARKETING = "marketing"
    const val DOCUMENTS = "documents"
    const val KNOWLEDGE = "knowledge"
    const val REPORTS = "reports"
    const val TEAM = "team"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"

    fun clientDetail(id: Long) = "client_detail/$id"
    fun projectDetail(id: Long) = "project_detail/$id"
    fun invoiceDetail(id: Long) = "invoice_detail/$id"
}
