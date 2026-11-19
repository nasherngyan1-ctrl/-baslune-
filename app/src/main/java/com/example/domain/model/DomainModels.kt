package com.example.domain.model

enum class CurrencyCode {
    IQD,
    USD
}

enum class UserRole {
    OWNER,
    ADMINISTRATOR,
    STAFF,
    FINANCE
}

enum class ClientStatus {
    POTENTIAL,
    CONTACTED,
    NEGOTIATING,
    ACTIVE,
    INACTIVE,
    COMPLETED,
    ARCHIVED
}

enum class ClientType {
    BUSINESS,
    COMPANY,
    ENTREPRENEUR,
    INDIVIDUAL,
    ONLINE
}

enum class ProjectCategory {
    WEBSITE_DEVELOPMENT,
    WEB_APPLICATIONS,
    MOBILE_APPLICATIONS,
    BRANDING_GRAPHIC_DESIGN,
    SOCIAL_MEDIA_MANAGEMENT,
    AI_CHATBOTS,
    BUSINESS_AUTOMATION,
    DIGITAL_CONSULTING,
    OTHER
}

enum class ProjectStatus {
    IDEA,
    NEW_REQUEST,
    PLANNING,
    IN_PROGRESS,
    WAITING_FOR_CLIENT,
    UNDER_REVIEW,
    REVISION,
    COMPLETED,
    CANCELLED,
    ARCHIVED
}

enum class PriorityLevel {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class TaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    WAITING,
    COMPLETED,
    CANCELLED
}

enum class ServiceCategory {
    WEBSITE_DESIGN_DEVELOPMENT,
    WEB_APPLICATIONS,
    MOBILE_APPLICATIONS,
    BRANDING_IDENTITY,
    GRAPHIC_DESIGN,
    SOCIAL_MEDIA_MANAGEMENT,
    AI_CHATBOTS,
    AI_AUTOMATION,
    DIGITAL_SOLUTIONS,
    BUSINESS_CONSULTING
}

enum class ServicePriceType {
    FIXED,
    HOURLY,
    CUSTOM_QUOTATION,
    PACKAGE
}

enum class OpportunityStage {
    NEW_LEAD,
    CONTACTED,
    DISCOVERY,
    PROPOSAL_SENT,
    NEGOTIATION,
    WON,
    LOST
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionCategory {
    // Income
    INCOME_WEBSITE_DEVELOPMENT,
    INCOME_APP_DEVELOPMENT,
    INCOME_BRANDING,
    INCOME_GRAPHIC_DESIGN,
    INCOME_SOCIAL_MEDIA,
    INCOME_AUTOMATION,
    INCOME_CONSULTING,
    INCOME_OTHER,
    // Expense
    EXPENSE_SOFTWARE,
    EXPENSE_SUBSCRIPTIONS,
    EXPENSE_ADVERTISING,
    EXPENSE_INTERNET,
    EXPENSE_TRANSPORTATION,
    EXPENSE_EQUIPMENT,
    EXPENSE_CONTRACTORS,
    EXPENSE_OFFICE,
    EXPENSE_OTHER;

    fun isIncome(): Boolean = name.startsWith("INCOME_")
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    CANCELLED
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    ZAIN_CASH,
    FASTPAY,
    FIB,
    CARD,
    OTHER
}

enum class InvoiceStatus {
    DRAFT,
    SENT,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED
}

enum class SocialChannel {
    INSTAGRAM,
    FACEBOOK,
    TIKTOK,
    WEBSITE,
    WHATSAPP
}

enum class ContentStatus {
    IDEA,
    DRAFT,
    SCHEDULED,
    PUBLISHED,
    ARCHIVED
}

enum class CampaignStatus {
    PLANNED,
    ACTIVE,
    PAUSED,
    COMPLETED
}

enum class DocumentCategory {
    CONTRACTS,
    INVOICES,
    RECEIPTS,
    PROJECT_FILES,
    BRANDING,
    COMPANY_DOCUMENTS,
    PROPOSALS,
    REPORTS,
    OTHER
}

enum class KnowledgeCategory {
    BUSINESS_STRATEGIES,
    PROCEDURES,
    INTERNAL_INSTRUCTIONS,
    CLIENT_TEMPLATES,
    PROJECT_CHECKLISTS,
    TECHNICAL_DOCS,
    IDEAS,
    MEETING_NOTES,
    DECISIONS,
    LESSONS_LEARNED
}

enum class DateRangePreset {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_QUARTER,
    THIS_YEAR,
    ALL_TIME,
    CUSTOM
}

enum class AppModule {
    DASHBOARD,
    CLIENTS,
    PROJECTS,
    TASKS,
    SERVICES,
    SALES,
    FINANCE,
    INVOICES,
    MARKETING,
    DOCUMENTS,
    KNOWLEDGE,
    REPORTS,
    TEAM,
    SETTINGS,
    BACKUP
}

data class CurrencyBreakdown(
    val iqd: Double = 0.0,
    val usd: Double = 0.0
) {
    fun combinedInIqd(usdToIqdRate: Double): Double = iqd + (usd * usdToIqdRate)
    fun combinedInUsd(usdToIqdRate: Double): Double =
        usd + if (usdToIqdRate > 0.0) (iqd / usdToIqdRate) else 0.0
}

data class FinancialSummary(
    val income: CurrencyBreakdown = CurrencyBreakdown(),
    val expenses: CurrencyBreakdown = CurrencyBreakdown(),
    val netProfit: CurrencyBreakdown = CurrencyBreakdown(),
    val outstandingReceivables: CurrencyBreakdown = CurrencyBreakdown(),
    val totalInvoiced: CurrencyBreakdown = CurrencyBreakdown(),
    val paidInvoicesCount: Int = 0,
    val unpaidOrOverdueInvoicesCount: Int = 0,
    val monthlyRevenueIqd: Double = 0.0,
    val monthlyRevenueUsd: Double = 0.0,
    val previousMonthRevenueIqd: Double = 0.0,
    val previousMonthRevenueUsd: Double = 0.0,
    val revenueGrowthPercent: Double? = null
)

data class ProjectProfitability(
    val projectId: Long,
    val incomeIqd: Double,
    val incomeUsd: Double,
    val expenseIqd: Double,
    val expenseUsd: Double,
    val profitIqd: Double,
    val profitUsd: Double
)

data class ComputedNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetRoute: String? = null
)

enum class NotificationType {
    OVERDUE_TASK,
    UPCOMING_DEADLINE,
    UNPAID_INVOICE,
    FOLLOW_UP_DUE,
    PROJECT_UPDATE,
    FINANCIAL_EVENT
}
