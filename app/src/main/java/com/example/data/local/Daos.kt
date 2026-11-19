package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BasluneDao {
    // Team Members
    @Query("SELECT * FROM team_members ORDER BY id ASC")
    fun observeTeamMembers(): Flow<List<UserMemberEntity>>

    @Query("SELECT * FROM team_members")
    suspend fun getAllTeamMembers(): List<UserMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeamMember(item: UserMemberEntity): Long

    @Query("DELETE FROM team_members WHERE id = :id")
    suspend fun deleteTeamMember(id: Long)

    // Clients
    @Query("SELECT * FROM clients ORDER BY dateAdded DESC")
    fun observeClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients")
    suspend fun getAllClients(): List<ClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClient(item: ClientEntity): Long

    @Query("DELETE FROM clients WHERE isSample = 1")
    suspend fun deleteSampleClients()

    // Client Communications
    @Query("SELECT * FROM client_communications ORDER BY date DESC")
    fun observeCommunications(): Flow<List<ClientCommunicationEntity>>

    @Query("SELECT * FROM client_communications")
    suspend fun getAllCommunications(): List<ClientCommunicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommunication(item: ClientCommunicationEntity): Long

    @Query("DELETE FROM client_communications WHERE isSample = 1")
    suspend fun deleteSampleCommunications()

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(item: ProjectEntity): Long

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM projects WHERE isSample = 1")
    suspend fun deleteSampleProjects()

    @Query("DELETE FROM project_milestones WHERE projectId = :projectId")
    suspend fun deleteMilestonesForProject(projectId: Long)

    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId")
    suspend fun unlinkTasksFromProject(projectId: Long)

    @Query("UPDATE opportunities SET convertedProjectId = NULL WHERE convertedProjectId = :projectId")
    suspend fun unlinkOpportunitiesFromProject(projectId: Long)

    @Query("UPDATE invoices SET projectId = NULL WHERE projectId = :projectId")
    suspend fun unlinkInvoicesFromProject(projectId: Long)

    @Query("UPDATE transactions SET projectId = NULL WHERE projectId = :projectId")
    suspend fun unlinkTransactionsFromProject(projectId: Long)

    @Query("UPDATE documents SET projectId = NULL WHERE projectId = :projectId")
    suspend fun unlinkDocumentsFromProject(projectId: Long)

    // Project Milestones
    @Query("SELECT * FROM project_milestones ORDER BY id ASC")
    fun observeMilestones(): Flow<List<ProjectMilestoneEntity>>

    @Query("SELECT * FROM project_milestones")
    suspend fun getAllMilestones(): List<ProjectMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(item: ProjectMilestoneEntity): Long

    @Query("DELETE FROM project_milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)

    @Query("DELETE FROM project_milestones WHERE isSample = 1")
    suspend fun deleteSampleMilestones()

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC, id DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(item: TaskEntity): Long

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM tasks WHERE isSample = 1")
    suspend fun deleteSampleTasks()

    // Services
    @Query("SELECT * FROM services ORDER BY id ASC")
    fun observeServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    suspend fun getAllServices(): List<ServiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertService(item: ServiceEntity): Long

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: Long)

    @Query("DELETE FROM services WHERE isSample = 1")
    suspend fun deleteSampleServices()

    // Opportunities
    @Query("SELECT * FROM opportunities ORDER BY updatedAt DESC")
    fun observeOpportunities(): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities")
    suspend fun getAllOpportunities(): List<OpportunityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOpportunity(item: OpportunityEntity): Long

    @Query("DELETE FROM opportunities WHERE id = :id")
    suspend fun deleteOpportunity(id: Long)

    @Query("DELETE FROM opportunities WHERE isSample = 1")
    suspend fun deleteSampleOpportunities()

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC, id DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoices(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoice(item: InvoiceEntity): Long

    @Query("SELECT * FROM invoice_items")
    fun observeInvoiceItems(): Flow<List<InvoiceItemEntity>>

    @Query("SELECT * FROM invoice_items")
    suspend fun getAllInvoiceItems(): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoiceItem(item: InvoiceItemEntity): Long

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(item: PaymentEntity): Long

    @Query("DELETE FROM invoice_items WHERE invoiceId IN (SELECT id FROM invoices WHERE isSample = 1)")
    suspend fun deleteSampleInvoiceItems()

    @Query("DELETE FROM payments WHERE isSample = 1")
    suspend fun deleteSamplePayments()

    @Query("DELETE FROM invoices WHERE isSample = 1")
    suspend fun deleteSampleInvoices()

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(item: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions WHERE isSample = 1")
    suspend fun deleteSampleTransactions()

    // Campaigns & Content
    @Query("SELECT * FROM campaigns ORDER BY startDate DESC")
    fun observeCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns")
    suspend fun getAllCampaigns(): List<CampaignEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCampaign(item: CampaignEntity): Long

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaign(id: Long)

    @Query("DELETE FROM campaigns WHERE isSample = 1")
    suspend fun deleteSampleCampaigns()

    @Query("SELECT * FROM content_items ORDER BY plannedDate ASC")
    fun observeContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items")
    suspend fun getAllContentItems(): List<ContentItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContentItem(item: ContentItemEntity): Long

    @Query("DELETE FROM content_items WHERE id = :id")
    suspend fun deleteContentItem(id: Long)

    @Query("DELETE FROM content_items WHERE isSample = 1")
    suspend fun deleteSampleContentItems()

    // Documents
    @Query("SELECT * FROM documents ORDER BY dateAdded DESC")
    fun observeDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents")
    suspend fun getAllDocuments(): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(item: DocumentEntity): Long

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)

    @Query("DELETE FROM documents WHERE isSample = 1")
    suspend fun deleteSampleDocuments()

    // Knowledge Articles
    @Query("SELECT * FROM knowledge_articles ORDER BY updatedAt DESC")
    fun observeKnowledgeArticles(): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_articles")
    suspend fun getAllKnowledgeArticles(): List<KnowledgeArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKnowledgeArticle(item: KnowledgeArticleEntity): Long

    @Query("DELETE FROM knowledge_articles WHERE id = :id")
    suspend fun deleteKnowledgeArticle(id: Long)

    @Query("DELETE FROM knowledge_articles WHERE isSample = 1")
    suspend fun deleteSampleKnowledgeArticles()

    // Activity Logs & Read Notifications
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun observeActivityLogs(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    suspend fun getAllActivityLogs(): List<ActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(item: ActivityLogEntity): Long

    @Query("SELECT notificationId FROM read_notifications")
    fun observeReadNotificationIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markNotificationRead(item: NotificationReadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markNotificationsRead(items: List<NotificationReadEntity>)

    // Clear all sample data atomically
    @Transaction
    suspend fun clearAllSampleData() {
        deleteSampleInvoiceItems()
        deleteSamplePayments()
        deleteSampleInvoices()
        deleteSampleTransactions()
        deleteSampleTasks()
        deleteSampleMilestones()
        deleteSampleProjects()
        deleteSampleCommunications()
        deleteSampleOpportunities()
        deleteSampleClients()
        deleteSampleServices()
        deleteSampleContentItems()
        deleteSampleCampaigns()
        deleteSampleDocuments()
        deleteSampleKnowledgeArticles()
    }
}
