package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.*

@Entity(tableName = "team_members")
data class UserMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String = "",
    val role: UserRole = UserRole.STAFF,
    val department: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientCode: String = "",
    val fullName: String,
    val companyName: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val country: String = "Iraq",
    val city: String = "Erbil",
    val address: String = "",
    val clientType: ClientType = ClientType.BUSINESS,
    val industry: String = "",
    val acquisitionSource: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val status: ClientStatus = ClientStatus.POTENTIAL,
    val notes: String = "",
    val tags: String = "",
    val isSample: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "client_communications")
data class ClientCommunicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val interactionType: String,
    val summary: String,
    val requirements: String = "",
    val followUpDate: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectCode: String = "",
    val name: String,
    val clientId: Long? = null,
    val description: String = "",
    val category: ProjectCategory = ProjectCategory.WEBSITE_DEVELOPMENT,
    val startDate: Long = System.currentTimeMillis(),
    val deadline: Long? = null,
    val budget: Double = 0.0,
    val agreedPrice: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.IQD,
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val priority: PriorityLevel = PriorityLevel.NORMAL,
    val assignedMembers: String = "",
    val progressPercent: Int = 0,
    val deliverables: String = "",
    val externalUrl: String = "",
    val classification: String = "",
    val internalNotes: String = "",
    val isStarter: Boolean = false,
    val isSample: Boolean = false,
    val isArchived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_milestones")
data class ProjectMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val isSample: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val clientId: Long? = null,
    val category: String = "",
    val priority: PriorityLevel = PriorityLevel.NORMAL,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val estimatedHours: Double = 0.0,
    val actualHours: Double = 0.0,
    val assignedTo: String = "Owner",
    val notes: String = "",
    val completionDate: Long? = null,
    val isRecurring: Boolean = false,
    val recurrenceLabel: String = "",
    val isSample: Boolean = false
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: ServiceCategory = ServiceCategory.WEBSITE_DESIGN_DEVELOPMENT,
    val priceType: ServicePriceType = ServicePriceType.FIXED,
    val startingPrice: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.IQD,
    val estimatedDelivery: String = "",
    val requiredInfo: String = "",
    val includedFeatures: String = "",
    val optionalAddOns: String = "",
    val isActive: Boolean = true,
    val isSample: Boolean = false
)

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val potentialClientName: String,
    val clientId: Long? = null,
    val requestedServiceId: Long? = null,
    val requestedServiceName: String = "",
    val estimatedValue: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.IQD,
    val expectedClosingDate: Long? = null,
    val source: String = "",
    val stage: OpportunityStage = OpportunityStage.NEW_LEAD,
    val probability: Int = 25,
    val notes: String = "",
    val nextFollowUpDate: Long? = null,
    val communicationNotes: String = "",
    val convertedClientId: Long? = null,
    val convertedProjectId: Long? = null,
    val isSample: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val clientId: Long? = null,
    val clientNameSnapshot: String,
    val projectId: Long? = null,
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000,
    val currency: CurrencyCode = CurrencyCode.IQD,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val notes: String = "",
    val isSample: Boolean = false
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val serviceId: Long? = null,
    val title: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val amount: Double,
    val currency: CurrencyCode,
    val paymentDate: Long = System.currentTimeMillis(),
    val method: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val isSample: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionCode: String = "",
    val type: TransactionType,
    val amount: Double,
    val currency: CurrencyCode = CurrencyCode.IQD,
    val category: TransactionCategory,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val clientId: Long? = null,
    val projectId: Long? = null,
    val linkedInvoiceId: Long? = null,
    val linkedPaymentId: Long? = null,
    val description: String = "",
    val receiptUrl: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val isSample: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val channel: SocialChannel = SocialChannel.INSTAGRAM,
    val status: CampaignStatus = CampaignStatus.PLANNED,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val adSpend: Double = 0.0,
    val currency: CurrencyCode = CurrencyCode.USD,
    val leadsGenerated: Int = 0,
    val notes: String = "",
    val isSample: Boolean = false
)

@Entity(tableName = "content_items")
data class ContentItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val platform: SocialChannel = SocialChannel.INSTAGRAM,
    val contentType: String = "Reel / Post",
    val caption: String = "",
    val plannedDate: Long = System.currentTimeMillis(),
    val status: ContentStatus = ContentStatus.IDEA,
    val mediaLink: String = "",
    val campaignId: Long? = null,
    val performanceNotes: String = "",
    val isSample: Boolean = false
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: DocumentCategory = DocumentCategory.PROJECT_FILES,
    val clientId: Long? = null,
    val projectId: Long? = null,
    val driveUrl: String,
    val description: String = "",
    val tags: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Entity(tableName = "knowledge_articles")
data class KnowledgeArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: KnowledgeCategory = KnowledgeCategory.PROCEDURES,
    val content: String,
    val tags: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val details: String,
    val actorRole: UserRole = UserRole.OWNER,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "read_notifications")
data class NotificationReadEntity(
    @PrimaryKey val notificationId: String,
    val readAt: Long = System.currentTimeMillis()
)
