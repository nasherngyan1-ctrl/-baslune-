package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.localization.AppLanguage
import com.example.core.security.BusinessPermission
import com.example.core.security.RolePermissions
import com.example.data.local.*
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface BasluneRepository {
    val settingsFlow: Flow<BusinessSettings>
    val teamMembers: Flow<List<UserMemberEntity>>
    val clients: Flow<List<ClientEntity>>
    val communications: Flow<List<ClientCommunicationEntity>>
    val projects: Flow<List<ProjectEntity>>
    val milestones: Flow<List<ProjectMilestoneEntity>>
    val tasks: Flow<List<TaskEntity>>
    val services: Flow<List<ServiceEntity>>
    val opportunities: Flow<List<OpportunityEntity>>
    val invoices: Flow<List<InvoiceEntity>>
    val invoiceItems: Flow<List<InvoiceItemEntity>>
    val payments: Flow<List<PaymentEntity>>
    val transactions: Flow<List<TransactionEntity>>
    val campaigns: Flow<List<CampaignEntity>>
    val contentItems: Flow<List<ContentItemEntity>>
    val documents: Flow<List<DocumentEntity>>
    val knowledgeArticles: Flow<List<KnowledgeArticleEntity>>
    val activityLogs: Flow<List<ActivityLogEntity>>
    val readNotificationIds: Flow<List<String>>

    suspend fun ensureInitialSeed()
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setExchangeRate(usdToIqd: Double)
    suspend fun setDefaultCurrency(currency: CurrencyCode)
    suspend fun setActiveRole(role: UserRole, userName: String? = null)
    suspend fun updateBusinessProfile(
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
    )

    suspend fun upsertClient(client: ClientEntity): Long
    suspend fun archiveClient(client: ClientEntity)
    suspend fun addCommunication(comm: ClientCommunicationEntity): Long

    suspend fun upsertProject(project: ProjectEntity): Long
    suspend fun duplicateProject(project: ProjectEntity): Long
    suspend fun archiveProject(project: ProjectEntity)
    suspend fun deleteProject(projectId: Long)
    suspend fun deleteStarterProject(projectId: Long)
    suspend fun upsertMilestone(milestone: ProjectMilestoneEntity): Long
    suspend fun toggleMilestone(milestone: ProjectMilestoneEntity)

    suspend fun upsertTask(task: TaskEntity): Long
    suspend fun toggleTaskCompleted(task: TaskEntity, completed: Boolean)
    suspend fun deleteTask(taskId: Long)

    suspend fun upsertService(service: ServiceEntity): Long
    suspend fun deleteService(serviceId: Long)

    suspend fun upsertOpportunity(opportunity: OpportunityEntity): Long
    suspend fun convertOpportunityToClientAndProject(opportunity: OpportunityEntity): Pair<Long, Long>
    suspend fun deleteOpportunity(opportunityId: Long)

    suspend fun upsertTransaction(transaction: TransactionEntity): Long
    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun saveInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long
    suspend fun markInvoiceSent(invoice: InvoiceEntity)
    suspend fun cancelInvoice(invoice: InvoiceEntity)
    suspend fun recordInvoicePayment(
        invoice: InvoiceEntity,
        amount: Double,
        method: PaymentMethod,
        notes: String
    )

    suspend fun upsertCampaign(campaign: CampaignEntity): Long
    suspend fun deleteCampaign(campaignId: Long)
    suspend fun upsertContentItem(item: ContentItemEntity): Long
    suspend fun deleteContentItem(itemId: Long)

    suspend fun upsertDocument(document: DocumentEntity): Long
    suspend fun deleteDocument(documentId: Long)

    suspend fun upsertKnowledgeArticle(article: KnowledgeArticleEntity): Long
    suspend fun deleteKnowledgeArticle(articleId: Long)

    suspend fun upsertTeamMember(member: UserMemberEntity): Long
    suspend fun deleteTeamMember(memberId: Long)

    suspend fun markNotificationRead(id: String)
    suspend fun markAllNotificationsRead(ids: List<String>)

    suspend fun activateSampleData()
    suspend fun deactivateSampleData()
    suspend fun recordLastBackup(timestamp: Long)
    suspend fun getDatabaseSnapshot(): DatabaseSnapshot
    suspend fun restoreSnapshot(snapshot: DatabaseSnapshot, replaceExisting: Boolean)
}

data class DatabaseSnapshot(
    val teamMembers: List<UserMemberEntity>,
    val clients: List<ClientEntity>,
    val communications: List<ClientCommunicationEntity>,
    val projects: List<ProjectEntity>,
    val milestones: List<ProjectMilestoneEntity>,
    val tasks: List<TaskEntity>,
    val services: List<ServiceEntity>,
    val opportunities: List<OpportunityEntity>,
    val invoices: List<InvoiceEntity>,
    val invoiceItems: List<InvoiceItemEntity>,
    val payments: List<PaymentEntity>,
    val transactions: List<TransactionEntity>,
    val campaigns: List<CampaignEntity>,
    val contentItems: List<ContentItemEntity>,
    val documents: List<DocumentEntity>,
    val knowledgeArticles: List<KnowledgeArticleEntity>
)

class LocalBasluneRepository(
    private val db: BasluneDatabase,
    private val dao: BasluneDao,
    private val settingsStore: SettingsDataStore,
    private val seeder: com.example.data.seed.StarterDataSeeder
) : BasluneRepository {

    override val settingsFlow = settingsStore.settingsFlow
    override val teamMembers = dao.observeTeamMembers()
    override val clients = dao.observeClients()
    override val communications = dao.observeCommunications()
    override val projects = dao.observeProjects()
    override val milestones = dao.observeMilestones()
    override val tasks = dao.observeTasks()
    override val services = dao.observeServices()
    override val opportunities = dao.observeOpportunities()
    override val invoices = dao.observeInvoices()
    override val invoiceItems = dao.observeInvoiceItems()
    override val payments = dao.observePayments()
    override val transactions = dao.observeTransactions()
    override val campaigns = dao.observeCampaigns()
    override val contentItems = dao.observeContentItems()
    override val documents = dao.observeDocuments()
    override val knowledgeArticles = dao.observeKnowledgeArticles()
    override val activityLogs = dao.observeActivityLogs()
    override val readNotificationIds = dao.observeReadNotificationIds()

    private suspend fun currentRole(): UserRole = settingsFlow.first().activeRole

    private suspend fun log(entityType: String, entityId: Long, action: String, details: String) {
        dao.insertActivityLog(
            ActivityLogEntity(
                entityType = entityType,
                entityId = entityId,
                action = action,
                details = details,
                actorRole = currentRole()
            )
        )
    }

    override suspend fun ensureInitialSeed() {
        val settings = settingsFlow.first()
        if (!settings.starterSeeded) {
            seeder.seedStarterFoundation(dao)
            settingsStore.setStarterSeeded(true)
        }
    }

    override suspend fun setLanguage(language: AppLanguage) = settingsStore.setLanguage(language)
    override suspend fun setDarkTheme(enabled: Boolean) = settingsStore.setDarkTheme(enabled)

    override suspend fun setExchangeRate(usdToIqd: Double) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_FINANCE)
        settingsStore.setExchangeRate(usdToIqd)
        log("SETTINGS", 0L, "EXCHANGE_RATE", "1 USD = $usdToIqd IQD")
    }

    override suspend fun setDefaultCurrency(currency: CurrencyCode) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_SETTINGS)
        settingsStore.setDefaultCurrency(currency)
    }

    override suspend fun setActiveRole(role: UserRole, userName: String?) {
        settingsStore.setActiveRole(role, userName)
    }

    override suspend fun updateBusinessProfile(
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
    ) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_SETTINGS)
        settingsStore.updateBusinessProfile(
            businessName, tagline, location, email, instagram, tiktok, streetAddress, phone, taxId, bankDetails
        )
    }

    override suspend fun upsertClient(client: ClientEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_CLIENTS)
        val isNew = client.id == 0L
        val code = client.clientCode.ifBlank { "CL-${System.currentTimeMillis().toString().takeLast(5)}" }
        val id = dao.upsertClient(client.copy(clientCode = code))
        log("CLIENT", id, if (isNew) "CREATED" else "UPDATED", client.fullName)
        return id
    }

    override suspend fun archiveClient(client: ClientEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.ARCHIVE_CLIENTS)
        dao.upsertClient(client.copy(isArchived = true, status = ClientStatus.ARCHIVED))
        log("CLIENT", client.id, "ARCHIVED", client.fullName)
    }

    override suspend fun addCommunication(comm: ClientCommunicationEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_CLIENTS)
        val id = dao.upsertCommunication(comm)
        log("CLIENT_COMM", comm.clientId, "LOGGED", "${comm.interactionType}: ${comm.summary}")
        return id
    }

    override suspend fun upsertProject(project: ProjectEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_PROJECTS)
        val isNew = project.id == 0L
        val code = project.projectCode.ifBlank { "PRJ-${System.currentTimeMillis().toString().takeLast(5)}" }
        val normalized = project.copy(
            projectCode = code,
            isArchived = project.status == ProjectStatus.ARCHIVED,
            updatedAt = System.currentTimeMillis()
        )
        val id = dao.upsertProject(normalized)
        log("PROJECT", id, if (isNew) "CREATED" else "UPDATED", "${normalized.name} (${normalized.status.name})")
        return id
    }

    override suspend fun duplicateProject(project: ProjectEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_PROJECTS)
        val copy = project.copy(
            id = 0L,
            projectCode = "PRJ-${System.currentTimeMillis().toString().takeLast(5)}",
            name = "${project.name} (Copy)",
            progressPercent = 0,
            status = ProjectStatus.PLANNING,
            isArchived = false,
            updatedAt = System.currentTimeMillis()
        )
        val id = dao.upsertProject(copy)
        log("PROJECT", id, "DUPLICATED", copy.name)
        return id
    }

    override suspend fun archiveProject(project: ProjectEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.ARCHIVE_PROJECTS)
        dao.upsertProject(project.copy(isArchived = true, status = ProjectStatus.ARCHIVED, updatedAt = System.currentTimeMillis()))
        log("PROJECT", project.id, "ARCHIVED", project.name)
    }

    override suspend fun deleteProject(projectId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.ARCHIVE_PROJECTS)
        db.withTransaction {
            val existing = dao.getProjectById(projectId)
            dao.deleteMilestonesForProject(projectId)
            dao.unlinkTasksFromProject(projectId)
            dao.unlinkOpportunitiesFromProject(projectId)
            dao.unlinkInvoicesFromProject(projectId)
            dao.unlinkTransactionsFromProject(projectId)
            dao.unlinkDocumentsFromProject(projectId)
            dao.deleteProjectById(projectId)
            log("PROJECT", projectId, "DELETED", existing?.name ?: "Project #$projectId removed")
        }
    }

    override suspend fun deleteStarterProject(projectId: Long) {
        deleteProject(projectId)
    }

    override suspend fun upsertMilestone(milestone: ProjectMilestoneEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_PROJECTS)
        val id = dao.upsertMilestone(milestone)
        log("MILESTONE", milestone.projectId, "ADDED", milestone.title)
        return id
    }

    override suspend fun toggleMilestone(milestone: ProjectMilestoneEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_PROJECTS)
        dao.upsertMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
        log("MILESTONE", milestone.projectId, "TOGGLED", milestone.title)
    }

    override suspend fun upsertTask(task: TaskEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_TASKS)
        val isNew = task.id == 0L
        val completion = if (task.status == TaskStatus.COMPLETED && task.completionDate == null) {
            System.currentTimeMillis()
        } else if (task.status != TaskStatus.COMPLETED) {
            null
        } else {
            task.completionDate
        }
        val id = dao.upsertTask(task.copy(completionDate = completion))
        task.projectId?.let { recalculateProjectProgress(it) }
        log("TASK", id, if (isNew) "CREATED" else "UPDATED", task.title)
        return id
    }

    override suspend fun toggleTaskCompleted(task: TaskEntity, completed: Boolean) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_TASKS)
        val newStatus = if (completed) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
        val completionDate = if (completed) System.currentTimeMillis() else null
        dao.upsertTask(task.copy(status = newStatus, completionDate = completionDate))
        task.projectId?.let { recalculateProjectProgress(it) }
        log("TASK", task.id, if (completed) "COMPLETED" else "REOPENED", task.title)
    }

    private suspend fun recalculateProjectProgress(projectId: Long) {
        val projectTasks = dao.getAllTasks().filter { it.projectId == projectId && it.status != TaskStatus.CANCELLED }
        if (projectTasks.isEmpty()) return
        val completedCount = projectTasks.count { it.status == TaskStatus.COMPLETED }
        val newProgress = ((completedCount.toDouble() / projectTasks.size) * 100.0).toInt().coerceIn(0, 100)
        val project = dao.getAllProjects().firstOrNull { it.id == projectId } ?: return
        dao.upsertProject(project.copy(progressPercent = newProgress, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteTask(taskId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_TASKS)
        dao.deleteTask(taskId)
        log("TASK", taskId, "DELETED", "Task #$taskId deleted")
    }

    override suspend fun upsertService(service: ServiceEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_SERVICES)
        return dao.upsertService(service)
    }

    override suspend fun deleteService(serviceId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_SERVICES)
        dao.deleteService(serviceId)
    }

    override suspend fun upsertOpportunity(opportunity: OpportunityEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_SALES)
        val id = dao.upsertOpportunity(opportunity.copy(updatedAt = System.currentTimeMillis()))
        log("OPPORTUNITY", id, "SAVED", "${opportunity.title} (${opportunity.stage.name})")
        return id
    }

    override suspend fun convertOpportunityToClientAndProject(opportunity: OpportunityEntity): Pair<Long, Long> {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_SALES)
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_CLIENTS)
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_PROJECTS)

        return db.withTransaction {
            val clientId = opportunity.clientId ?: dao.upsertClient(
                ClientEntity(
                    clientCode = "CL-${System.currentTimeMillis().toString().takeLast(5)}",
                    fullName = opportunity.potentialClientName,
                    acquisitionSource = opportunity.source,
                    status = ClientStatus.ACTIVE,
                    notes = opportunity.notes,
                    isSample = opportunity.isSample
                )
            )
            val projectId = dao.upsertProject(
                ProjectEntity(
                    projectCode = "PRJ-${System.currentTimeMillis().toString().takeLast(5)}",
                    name = opportunity.title,
                    clientId = clientId,
                    description = buildString {
                        if (opportunity.requestedServiceName.isNotBlank()) {
                            append("Service: ${opportunity.requestedServiceName}\n")
                        }
                        append(opportunity.notes)
                    },
                    agreedPrice = opportunity.estimatedValue,
                    budget = opportunity.estimatedValue,
                    currency = opportunity.currency,
                    deadline = opportunity.expectedClosingDate,
                    status = ProjectStatus.PLANNING,
                    isSample = opportunity.isSample
                )
            )
            dao.upsertOpportunity(
                opportunity.copy(
                    stage = OpportunityStage.WON,
                    probability = 100,
                    clientId = clientId,
                    convertedClientId = clientId,
                    convertedProjectId = projectId,
                    updatedAt = System.currentTimeMillis()
                )
            )
            log("OPPORTUNITY", opportunity.id, "CONVERTED", "Client #$clientId -> Project #$projectId")
            clientId to projectId
        }
    }

    override suspend fun deleteOpportunity(opportunityId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_SALES)
        dao.deleteOpportunity(opportunityId)
    }

    override suspend fun upsertTransaction(transaction: TransactionEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_FINANCE)
        val isNew = transaction.id == 0L
        val code = transaction.transactionCode.ifBlank { "TX-${System.currentTimeMillis().toString().takeLast(6)}" }
        val id = dao.upsertTransaction(
            transaction.copy(transactionCode = code, updatedAt = System.currentTimeMillis())
        )
        log("FINANCE", id, if (isNew) "RECORDED" else "EDITED", "${transaction.type.name} ${transaction.amount} ${transaction.currency.name}")
        return id
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.DELETE_FINANCE)
        dao.deleteTransaction(transaction.id)
        log("FINANCE", transaction.id, "DELETED", "Deleted ${transaction.transactionCode} (${transaction.amount} ${transaction.currency.name})")
    }

    override suspend fun saveInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_INVOICES)
        val (subtotal, clampedDiscount, total) = FinancialCalculator.computeInvoiceTotals(
            itemTotals = items.map { it.quantity * it.unitPrice },
            discount = invoice.discount,
            tax = invoice.tax
        )
        val resolvedStatus = FinancialCalculator.resolveInvoiceStatus(
            currentStatus = invoice.status,
            total = total,
            paidAmount = invoice.paidAmount,
            dueDate = invoice.dueDate
        )
        val number = invoice.invoiceNumber.ifBlank {
            "INV-${System.currentTimeMillis().toString().takeLast(6)}"
        }
        return db.withTransaction {
            val savedId = dao.upsertInvoice(
                invoice.copy(
                    invoiceNumber = number,
                    subtotal = subtotal,
                    discount = clampedDiscount,
                    total = total,
                    status = resolvedStatus
                )
            )
            dao.deleteItemsForInvoice(savedId)
            items.forEach { item ->
                dao.upsertInvoiceItem(
                    item.copy(
                        id = 0L,
                        invoiceId = savedId,
                        lineTotal = item.quantity * item.unitPrice
                    )
                )
            }
            log("INVOICE", savedId, "SAVED", "$number Total: $total ${invoice.currency.name}")
            savedId
        }
    }

    override suspend fun markInvoiceSent(invoice: InvoiceEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_INVOICES)
        val status = FinancialCalculator.resolveInvoiceStatus(
            currentStatus = InvoiceStatus.SENT,
            total = invoice.total,
            paidAmount = invoice.paidAmount,
            dueDate = invoice.dueDate
        )
        dao.upsertInvoice(invoice.copy(status = status))
        log("INVOICE", invoice.id, "SENT", invoice.invoiceNumber)
    }

    override suspend fun cancelInvoice(invoice: InvoiceEntity) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_INVOICES)
        dao.upsertInvoice(invoice.copy(status = InvoiceStatus.CANCELLED))
        log("INVOICE", invoice.id, "CANCELLED", invoice.invoiceNumber)
    }

    override suspend fun recordInvoicePayment(
        invoice: InvoiceEntity,
        amount: Double,
        method: PaymentMethod,
        notes: String
    ) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.RECORD_PAYMENTS)
        require(amount > 0.0) { "Payment amount must be positive" }
        val remaining = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)
        val appliedAmount = amount.coerceAtMost(remaining)
        require(appliedAmount > 0.0) { "Invoice is already paid in full" }

        db.withTransaction {
            val now = System.currentTimeMillis()
            val paymentId = dao.upsertPayment(
                PaymentEntity(
                    invoiceId = invoice.id,
                    amount = appliedAmount,
                    currency = invoice.currency,
                    paymentDate = now,
                    method = method,
                    notes = notes,
                    isSample = invoice.isSample
                )
            )
            val newPaid = invoice.paidAmount + appliedAmount
            val newStatus = FinancialCalculator.resolveInvoiceStatus(
                currentStatus = InvoiceStatus.SENT,
                total = invoice.total,
                paidAmount = newPaid,
                dueDate = invoice.dueDate,
                nowMillis = now
            )
            dao.upsertInvoice(invoice.copy(paidAmount = newPaid, status = newStatus))

            // Single authoritative cash transaction linked to this payment (no double counting!)
            dao.upsertTransaction(
                TransactionEntity(
                    transactionCode = "PAY-${invoice.invoiceNumber}-${paymentId}",
                    type = TransactionType.INCOME,
                    amount = appliedAmount,
                    currency = invoice.currency,
                    category = TransactionCategory.INCOME_OTHER,
                    date = now,
                    paymentMethod = method,
                    clientId = invoice.clientId,
                    projectId = invoice.projectId,
                    linkedInvoiceId = invoice.id,
                    linkedPaymentId = paymentId,
                    description = "Invoice ${invoice.invoiceNumber} (${invoice.clientNameSnapshot})",
                    status = TransactionStatus.COMPLETED,
                    isSample = invoice.isSample,
                    updatedAt = now
                )
            )
            log("PAYMENT", invoice.id, "PAYMENT_RECORDED", "${invoice.invoiceNumber}: +$appliedAmount ${invoice.currency.name}")
        }
    }

    override suspend fun upsertCampaign(campaign: CampaignEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_MARKETING)
        return dao.upsertCampaign(campaign)
    }

    override suspend fun deleteCampaign(campaignId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_MARKETING)
        dao.deleteCampaign(campaignId)
    }

    override suspend fun upsertContentItem(item: ContentItemEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_MARKETING)
        return dao.upsertContentItem(item)
    }

    override suspend fun deleteContentItem(itemId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_MARKETING)
        dao.deleteContentItem(itemId)
    }

    override suspend fun upsertDocument(document: DocumentEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_DOCUMENTS)
        return dao.upsertDocument(document)
    }

    override suspend fun deleteDocument(documentId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_DOCUMENTS)
        dao.deleteDocument(documentId)
    }

    override suspend fun upsertKnowledgeArticle(article: KnowledgeArticleEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_KNOWLEDGE)
        return dao.upsertKnowledgeArticle(article.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteKnowledgeArticle(articleId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.EDIT_KNOWLEDGE)
        dao.deleteKnowledgeArticle(articleId)
    }

    override suspend fun upsertTeamMember(member: UserMemberEntity): Long {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_TEAM)
        return dao.upsertTeamMember(member)
    }

    override suspend fun deleteTeamMember(memberId: Long) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_TEAM)
        dao.deleteTeamMember(memberId)
    }

    override suspend fun markNotificationRead(id: String) {
        dao.markNotificationRead(NotificationReadEntity(notificationId = id))
    }

    override suspend fun markAllNotificationsRead(ids: List<String>) {
        dao.markNotificationsRead(ids.map { NotificationReadEntity(notificationId = it) })
    }

    override suspend fun activateSampleData() {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_SETTINGS)
        val settings = settingsFlow.first()
        if (!settings.sampleDataActive) {
            db.withTransaction {
                seeder.seedSampleBusinessData(dao)
            }
            settingsStore.setSampleDataActive(true)
            log("SETTINGS", 0L, "SAMPLE_DATA_ON", "Sample business data activated")
        }
    }

    override suspend fun deactivateSampleData() {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.MANAGE_SETTINGS)
        dao.clearAllSampleData()
        settingsStore.setSampleDataActive(false)
        log("SETTINGS", 0L, "SAMPLE_DATA_OFF", "Sample business data removed")
    }

    override suspend fun recordLastBackup(timestamp: Long) {
        settingsStore.setLastBackupTimestamp(timestamp)
    }

    override suspend fun getDatabaseSnapshot(): DatabaseSnapshot {
        return DatabaseSnapshot(
            teamMembers = dao.getAllTeamMembers(),
            clients = dao.getAllClients(),
            communications = dao.getAllCommunications(),
            projects = dao.getAllProjects(),
            milestones = dao.getAllMilestones(),
            tasks = dao.getAllTasks(),
            services = dao.getAllServices(),
            opportunities = dao.getAllOpportunities(),
            invoices = dao.getAllInvoices(),
            invoiceItems = dao.getAllInvoiceItems(),
            payments = dao.getAllPayments(),
            transactions = dao.getAllTransactions(),
            campaigns = dao.getAllCampaigns(),
            contentItems = dao.getAllContentItems(),
            documents = dao.getAllDocuments(),
            knowledgeArticles = dao.getAllKnowledgeArticles()
        )
    }

    override suspend fun restoreSnapshot(snapshot: DatabaseSnapshot, replaceExisting: Boolean) {
        RolePermissions.requirePermission(currentRole(), BusinessPermission.BACKUP_RESTORE)
        db.withTransaction {
            if (replaceExisting) {
                db.clearAllTables()
            }
            snapshot.teamMembers.forEach { dao.upsertTeamMember(it) }
            snapshot.clients.forEach { dao.upsertClient(it) }
            snapshot.communications.forEach { dao.upsertCommunication(it) }
            snapshot.projects.forEach { dao.upsertProject(it) }
            snapshot.milestones.forEach { dao.upsertMilestone(it) }
            snapshot.tasks.forEach { dao.upsertTask(it) }
            snapshot.services.forEach { dao.upsertService(it) }
            snapshot.opportunities.forEach { dao.upsertOpportunity(it) }
            snapshot.invoices.forEach { dao.upsertInvoice(it) }
            snapshot.invoiceItems.forEach { dao.upsertInvoiceItem(it) }
            snapshot.payments.forEach { dao.upsertPayment(it) }
            snapshot.transactions.forEach { dao.upsertTransaction(it) }
            snapshot.campaigns.forEach { dao.upsertCampaign(it) }
            snapshot.contentItems.forEach { dao.upsertContentItem(it) }
            snapshot.documents.forEach { dao.upsertDocument(it) }
            snapshot.knowledgeArticles.forEach { dao.upsertKnowledgeArticle(it) }
            log("BACKUP", 0L, "RESTORED", "Database snapshot restored (replaceExisting=$replaceExisting)")
        }
    }
}
