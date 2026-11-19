package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.core.localization.stringsFor
import com.example.core.security.RolePermissions
import com.example.data.local.*
import com.example.data.repository.BackupManager
import com.example.data.repository.BasluneRepository
import com.example.domain.logic.FinancialCalculator
import com.example.domain.logic.NotificationEngine
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BasluneUiState(
    val isReady: Boolean = false,
    val settings: BusinessSettings = BusinessSettings(),
    val dateRangePreset: DateRangePreset = DateRangePreset.ALL_TIME,
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null,
    val teamMembers: List<UserMemberEntity> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val communications: List<ClientCommunicationEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val milestones: List<ProjectMilestoneEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val services: List<ServiceEntity> = emptyList(),
    val opportunities: List<OpportunityEntity> = emptyList(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val invoiceItems: List<InvoiceItemEntity> = emptyList(),
    val payments: List<PaymentEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val campaigns: List<CampaignEntity> = emptyList(),
    val contentItems: List<ContentItemEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList(),
    val knowledgeArticles: List<KnowledgeArticleEntity> = emptyList(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val readNotificationIds: Set<String> = emptySet(),
    val notifications: List<ComputedNotification> = emptyList(),
    val financialSummary: FinancialSummary = FinancialSummary(),
    val snackbarMessage: String? = null
) {
    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead }

    fun canAccess(module: AppModule): Boolean =
        RolePermissions.canAccessModule(settings.activeRole, module)
}

class BasluneMainViewModel(
    private val repository: BasluneRepository
) : ViewModel() {

    private val dateFilterState = MutableStateFlow(Triple(DateRangePreset.ALL_TIME, null as Long?, null as Long?))
    private val snackbarState = MutableStateFlow<String?>(null)
    private val readyState = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.ensureInitialSeed()
            readyState.value = true
        }
    }

    private val coreDataFlow = combine(
        repository.settingsFlow,
        repository.teamMembers,
        repository.clients,
        repository.communications,
        repository.projects
    ) { settings, team, clients, comms, projects ->
        CoreBundleA(settings, team, clients, comms, projects)
    }

    private val coreDataFlowB = combine(
        repository.milestones,
        repository.tasks,
        repository.services,
        repository.opportunities,
        repository.invoices
    ) { milestones, tasks, services, opportunities, invoices ->
        CoreBundleB(milestones, tasks, services, opportunities, invoices)
    }

    private val coreDataFlowC = combine(
        repository.invoiceItems,
        repository.payments,
        repository.transactions,
        repository.campaigns,
        repository.contentItems
    ) { items, payments, tx, campaigns, content ->
        CoreBundleC(items, payments, tx, campaigns, content)
    }

    private val coreDataFlowD = combine(
        repository.documents,
        repository.knowledgeArticles,
        repository.activityLogs,
        repository.readNotificationIds,
        dateFilterState
    ) { docs, knowledge, logs, readIds, dateFilter ->
        CoreBundleD(docs, knowledge, logs, readIds.toSet(), dateFilter)
    }

    val uiState: StateFlow<BasluneUiState> = combine(
        coreDataFlow,
        coreDataFlowB,
        coreDataFlowC,
        coreDataFlowD,
        combine(readyState, snackbarState) { r, s -> r to s }
    ) { a, b, c, d, (ready, snackbar) ->
        val (preset, customStart, customEnd) = d.dateFilter
        val strings = stringsFor(a.settings.language)
        val summary = FinancialCalculator.calculateSummary(
            transactions = c.transactions,
            invoices = b.invoices,
            preset = preset,
            customStart = customStart,
            customEnd = customEnd,
            usdToIqdRate = a.settings.usdToIqdRate
        )
        val notifications = NotificationEngine.generateRealNotifications(
            tasks = b.tasks,
            projects = a.projects,
            invoices = b.invoices,
            opportunities = b.opportunities,
            communications = a.comms,
            readIds = d.readIds,
            strings = strings,
            language = a.settings.language
        )

        BasluneUiState(
            isReady = ready,
            settings = a.settings,
            dateRangePreset = preset,
            customStartMillis = customStart,
            customEndMillis = customEnd,
            teamMembers = a.team,
            clients = a.clients,
            communications = a.comms,
            projects = a.projects,
            milestones = b.milestones,
            tasks = b.tasks,
            services = b.services,
            opportunities = b.opportunities,
            invoices = b.invoices,
            invoiceItems = c.invoiceItems,
            payments = c.payments,
            transactions = c.transactions,
            campaigns = c.campaigns,
            contentItems = c.content,
            documents = d.docs,
            knowledgeArticles = d.knowledge,
            activityLogs = d.logs,
            readNotificationIds = d.readIds,
            notifications = notifications,
            financialSummary = summary,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BasluneUiState()
    )

    fun clearSnackbar() {
        snackbarState.value = null
    }

    fun showMessage(msg: String) {
        snackbarState.value = msg
    }

    private fun launchSafe(onSuccessMsg: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                if (onSuccessMsg != null) {
                    snackbarState.value = onSuccessMsg
                }
            } catch (e: SecurityException) {
                snackbarState.value = stringsFor(uiState.value.settings.language).accessDenied
            } catch (e: Exception) {
                snackbarState.value = e.message ?: "Error"
            }
        }
    }

    fun setDateRangePreset(preset: DateRangePreset, customStart: Long? = null, customEnd: Long? = null) {
        dateFilterState.value = Triple(preset, customStart, customEnd)
    }

    fun setLanguage(language: AppLanguage) = launchSafe { repository.setLanguage(language) }
    fun setDarkTheme(enabled: Boolean) = launchSafe { repository.setDarkTheme(enabled) }
    fun setExchangeRate(rate: Double) = launchSafe { repository.setExchangeRate(rate) }
    fun setDefaultCurrency(currency: CurrencyCode) = launchSafe { repository.setDefaultCurrency(currency) }
    fun setActiveRole(role: UserRole, userName: String? = null) = launchSafe { repository.setActiveRole(role, userName) }

    fun updateBusinessProfile(
        businessName: String,
        tagline: String,
        location: String,
        email: String,
        instagram: String,
        tiktok: String,
        streetAddress: String,
        phone: String,
        taxId: String,
        bankDetails: String
    ) = launchSafe {
        repository.updateBusinessProfile(
            businessName, tagline, location, email, instagram, tiktok, streetAddress, phone, taxId, bankDetails
        )
    }

    fun saveClient(client: ClientEntity) = launchSafe { repository.upsertClient(client) }
    fun archiveClient(client: ClientEntity) = launchSafe { repository.archiveClient(client) }
    fun addCommunication(comm: ClientCommunicationEntity) = launchSafe { repository.addCommunication(comm) }

    fun saveProject(project: ProjectEntity, successMsg: String? = null) = launchSafe(successMsg) {
        repository.upsertProject(project)
    }
    fun duplicateProject(project: ProjectEntity) = launchSafe { repository.duplicateProject(project) }
    fun archiveProject(project: ProjectEntity) = launchSafe { repository.archiveProject(project) }
    fun deleteProject(projectId: Long, successMsg: String? = null) = launchSafe(successMsg) {
        repository.deleteProject(projectId)
    }
    fun deleteStarterProject(projectId: Long) = launchSafe { repository.deleteProject(projectId) }
    fun addMilestone(milestone: ProjectMilestoneEntity) = launchSafe { repository.upsertMilestone(milestone) }
    fun toggleMilestone(milestone: ProjectMilestoneEntity) = launchSafe { repository.toggleMilestone(milestone) }

    fun saveTask(task: TaskEntity) = launchSafe { repository.upsertTask(task) }
    fun toggleTaskCompleted(task: TaskEntity, completed: Boolean) = launchSafe { repository.toggleTaskCompleted(task, completed) }
    fun deleteTask(taskId: Long) = launchSafe { repository.deleteTask(taskId) }

    fun saveService(service: ServiceEntity) = launchSafe { repository.upsertService(service) }
    fun deleteService(serviceId: Long) = launchSafe { repository.deleteService(serviceId) }

    fun saveOpportunity(opportunity: OpportunityEntity) = launchSafe { repository.upsertOpportunity(opportunity) }
    fun convertOpportunity(opportunity: OpportunityEntity, successMsg: String) = launchSafe(successMsg) {
        repository.convertOpportunityToClientAndProject(opportunity)
    }
    fun deleteOpportunity(opportunityId: Long) = launchSafe { repository.deleteOpportunity(opportunityId) }

    fun saveTransaction(transaction: TransactionEntity) = launchSafe { repository.upsertTransaction(transaction) }
    fun deleteTransaction(transaction: TransactionEntity) = launchSafe { repository.deleteTransaction(transaction) }

    fun saveInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) = launchSafe {
        repository.saveInvoiceWithItems(invoice, items)
    }
    fun markInvoiceSent(invoice: InvoiceEntity) = launchSafe { repository.markInvoiceSent(invoice) }
    fun cancelInvoice(invoice: InvoiceEntity) = launchSafe { repository.cancelInvoice(invoice) }
    fun recordInvoicePayment(invoice: InvoiceEntity, amount: Double, method: PaymentMethod, notes: String) = launchSafe {
        repository.recordInvoicePayment(invoice, amount, method, notes)
    }

    fun saveCampaign(campaign: CampaignEntity) = launchSafe { repository.upsertCampaign(campaign) }
    fun deleteCampaign(id: Long) = launchSafe { repository.deleteCampaign(id) }
    fun saveContentItem(item: ContentItemEntity) = launchSafe { repository.upsertContentItem(item) }
    fun deleteContentItem(id: Long) = launchSafe { repository.deleteContentItem(id) }

    fun saveDocument(document: DocumentEntity) = launchSafe { repository.upsertDocument(document) }
    fun deleteDocument(id: Long) = launchSafe { repository.deleteDocument(id) }

    fun saveKnowledgeArticle(article: KnowledgeArticleEntity) = launchSafe { repository.upsertKnowledgeArticle(article) }
    fun deleteKnowledgeArticle(id: Long) = launchSafe { repository.deleteKnowledgeArticle(id) }

    fun saveTeamMember(member: UserMemberEntity) = launchSafe { repository.upsertTeamMember(member) }
    fun deleteTeamMember(id: Long) = launchSafe { repository.deleteTeamMember(id) }

    fun markNotificationRead(id: String) = launchSafe { repository.markNotificationRead(id) }
    fun markAllNotificationsRead() = launchSafe {
        repository.markAllNotificationsRead(uiState.value.notifications.map { it.id })
    }

    fun activateSampleData() = launchSafe { repository.activateSampleData() }
    fun deactivateSampleData() = launchSafe { repository.deactivateSampleData() }

    suspend fun generateBackupJson(selectedModules: Set<AppModule>?): String {
        val snapshot = repository.getDatabaseSnapshot()
        val now = System.currentTimeMillis()
        val json = BackupManager.exportToJson(snapshot, selectedModules, now)
        repository.recordLastBackup(now)
        return json
    }

    fun restoreFromBackup(jsonText: String, replaceExisting: Boolean, successMsg: String, errorMsg: String) {
        viewModelScope.launch {
            when (val result = BackupManager.validateAndParse(jsonText)) {
                is BackupManager.ValidationResult.Valid -> {
                    try {
                        repository.restoreSnapshot(result.snapshot, replaceExisting)
                        snackbarState.value = successMsg
                    } catch (e: SecurityException) {
                        snackbarState.value = stringsFor(uiState.value.settings.language).accessDenied
                    } catch (e: Exception) {
                        snackbarState.value = e.message ?: errorMsg
                    }
                }
                is BackupManager.ValidationResult.Invalid -> {
                    snackbarState.value = "$errorMsg (${result.reason})"
                }
            }
        }
    }

    private data class CoreBundleA(
        val settings: BusinessSettings,
        val team: List<UserMemberEntity>,
        val clients: List<ClientEntity>,
        val comms: List<ClientCommunicationEntity>,
        val projects: List<ProjectEntity>
    )

    private data class CoreBundleB(
        val milestones: List<ProjectMilestoneEntity>,
        val tasks: List<TaskEntity>,
        val services: List<ServiceEntity>,
        val opportunities: List<OpportunityEntity>,
        val invoices: List<InvoiceEntity>
    )

    private data class CoreBundleC(
        val invoiceItems: List<InvoiceItemEntity>,
        val payments: List<PaymentEntity>,
        val transactions: List<TransactionEntity>,
        val campaigns: List<CampaignEntity>,
        val content: List<ContentItemEntity>
    )

    private data class CoreBundleD(
        val docs: List<DocumentEntity>,
        val knowledge: List<KnowledgeArticleEntity>,
        val logs: List<ActivityLogEntity>,
        val readIds: Set<String>,
        val dateFilter: Triple<DateRangePreset, Long?, Long?>
    )

    class Factory(private val repository: BasluneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BasluneMainViewModel(repository) as T
        }
    }
}
