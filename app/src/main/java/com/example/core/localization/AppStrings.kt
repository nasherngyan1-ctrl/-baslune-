package com.example.core.localization

import com.example.domain.model.*

interface AppStrings {
    // Brand & Shell
    val appName: String
    val studioName: String
    val tagline: String
    val locationErbil: String
    val searchPlaceholder: String
    val quickActions: String
    val notifications: String
    val noNotifications: String
    val markAllRead: String
    val profileAndRole: String
    val activeRole: String
    val allModules: String
    val back: String
    val close: String
    val save: String
    val cancel: String
    val edit: String
    val delete: String
    val archive: String
    val restore: String
    val duplicate: String
    val add: String
    val create: String
    val confirm: String
    val search: String
    val filter: String
    val sort: String
    val all: String
    val none: String
    val yes: String
    val no: String
    val sampleBadge: String
    val starterBadge: String
    val accessDenied: String
    val invalidInput: String
    val openLink: String
    val invalidUrl: String
    val copiedOrSaved: String

    // Navigation Modules
    val navDashboard: String
    val navClients: String
    val navProjects: String
    val navTasks: String
    val navServices: String
    val navSales: String
    val navFinance: String
    val navInvoices: String
    val navMarketing: String
    val navDocuments: String
    val navKnowledge: String
    val navReports: String
    val navTeam: String
    val navSettings: String
    val navBackup: String

    // Dashboard
    val dashboardSubtitle: String
    val financialOverview: String
    val businessOverview: String
    val actionCenter: String
    val chartsOverview: String
    val totalIncome: String
    val totalExpenses: String
    val netProfit: String
    val outstandingPayments: String
    val monthlyRevenue: String
    val revenueGrowth: String
    val totalClients: String
    val newClients: String
    val activeProjects: String
    val completedProjects: String
    val pendingProjects: String
    val overdueTasks: String
    val todaysTasks: String
    val upcomingDeadlines: String
    val overdueInvoices: String
    val pendingClientResponses: String
    val recentProjects: String
    val importantReminders: String
    val combinedEquivalentLabel: String
    val manualRateNote: String
    val activateSampleData: String
    val deactivateSampleData: String
    val sampleDataBannerTitle: String
    val sampleDataBannerDesc: String

    // Date Filters
    val dateToday: String
    val dateThisWeek: String
    val dateThisMonth: String
    val dateThisQuarter: String
    val dateThisYear: String
    val dateAllTime: String
    val dateCustom: String

    // Quick Actions
    val qaAddClient: String
    val qaAddProject: String
    val qaAddTask: String
    val qaRecordIncome: String
    val qaRecordExpense: String
    val qaCreateInvoice: String
    val qaAddOpportunity: String

    // Clients
    val addClient: String
    val editClient: String
    val clientDetails: String
    val clientName: String
    val companyName: String
    val email: String
    val phone: String
    val whatsapp: String
    val country: String
    val city: String
    val address: String
    val clientType: String
    val industry: String
    val acquisitionSource: String
    val tags: String
    val notes: String
    val relatedProjects: String
    val financialHistory: String
    val communicationHistory: String
    val recordCommunication: String
    val communicationType: String
    val followUpDate: String
    val requirements: String
    val emptyClientsTitle: String
    val emptyClientsDesc: String
    val archiveClientConfirm: String

    // Projects
    val addProject: String
    val editProject: String
    val projectDetails: String
    val projectName: String
    val description: String
    val category: String
    val status: String
    val priority: String
    val startDate: String
    val deadline: String
    val budget: String
    val agreedPrice: String
    val actualExpenses: String
    val progress: String
    val client: String
    val viewList: String
    val viewCards: String
    val viewKanban: String
    val viewCalendar: String
    val milestones: String
    val addMilestone: String
    val deliverables: String
    val activityLog: String
    val profitability: String
    val collectedIncome: String
    val emptyProjectsTitle: String
    val emptyProjectsDesc: String
    val externalUrl: String
    val classificationLabel: String
    val deleteProjectConfirm: String

    // Tasks
    val addTask: String
    val editTask: String
    val taskTitle: String
    val dueDate: String
    val estimatedHours: String
    val actualHours: String
    val assignedTo: String
    val recurringDailyWeekly: String
    val filterMyTasks: String
    val filterToday: String
    val filterUpcoming: String
    val filterOverdue: String
    val filterCompleted: String
    val filterByProject: String
    val filterByPriority: String
    val emptyTasksTitle: String
    val emptyTasksDesc: String
    val overdueWarning: String

    // Services
    val addService: String
    val editService: String
    val serviceName: String
    val priceType: String
    val startingPrice: String
    val deliveryTime: String
    val requiredInfo: String
    val includedFeatures: String
    val optionalAddOns: String
    val activeService: String
    val inactiveService: String
    val emptyServicesTitle: String
    val emptyServicesDesc: String

    // Sales & Opportunities
    val addOpportunity: String
    val editOpportunity: String
    val opportunityTitle: String
    val potentialClient: String
    val requestedService: String
    val estimatedValue: String
    val expectedClosingDate: String
    val probability: String
    val currentStage: String
    val pipelineValue: String
    val conversionRate: String
    val convertToClientProject: String
    val convertedSuccess: String
    val emptySalesTitle: String
    val emptySalesDesc: String

    // Finance
    val recordIncome: String
    val recordExpense: String
    val editTransaction: String
    val amount: String
    val currency: String
    val paymentMethod: String
    val date: String
    val receiptLink: String
    val exchangeRateTitle: String
    val usdToIqdRate: String
    val updateRate: String
    val separateCurrenciesNote: String
    val showCombinedEstimate: String
    val emptyFinanceTitle: String
    val emptyFinanceDesc: String
    val deleteTransactionConfirm: String
    val auditHistory: String

    // Invoices & Payments
    val createInvoice: String
    val editInvoice: String
    val invoiceDetails: String
    val invoiceNumber: String
    val issueDate: String
    val lineItems: String
    val addLineItem: String
    val serviceOrItem: String
    val quantity: String
    val unitPrice: String
    val discount: String
    val tax: String
    val subtotal: String
    val total: String
    val paidAmount: String
    val remainingBalance: String
    val recordPayment: String
    val paymentsHistory: String
    val printOrExportPdf: String
    val markAsSent: String
    val emptyInvoicesTitle: String
    val emptyInvoicesDesc: String

    // Marketing
    val addCampaign: String
    val addContentItem: String
    val campaignsTab: String
    val contentCalendarTab: String
    val campaignName: String
    val channel: String
    val contentType: String
    val caption: String
    val plannedDate: String
    val mediaLink: String
    val adSpend: String
    val leadsGenerated: String
    val performanceNotes: String
    val emptyMarketingTitle: String
    val emptyMarketingDesc: String

    // Documents
    val addDocument: String
    val documentTitle: String
    val driveUrl: String
    val driveUrlHint: String
    val driveStorageNotice: String
    val emptyDocumentsTitle: String
    val emptyDocumentsDesc: String

    // Knowledge
    val addArticle: String
    val articleTitle: String
    val articleContent: String
    val emptyKnowledgeTitle: String
    val emptyKnowledgeDesc: String

    // Reports
    val exportCsv: String
    val financialReport: String
    val monthlyIncomeReport: String
    val expenseReport: String
    val profitReport: String
    val clientReport: String
    val projectReport: String
    val serviceReport: String
    val salesReport: String
    val taskReport: String
    val marketingReport: String
    val noDataForRange: String

    // Team
    val addMember: String
    val memberName: String
    val memberRole: String
    val memberDepartment: String
    val switchActiveRoleHint: String
    val permissionsMatrix: String

    // Settings & Backup
    val businessProfile: String
    val languageSettings: String
    val appearanceSettings: String
    val darkMode: String
    val currencyAndRate: String
    val sampleDataControl: String
    val localFirstSecurityTitle: String
    val localFirstSecurityDesc: String
    val backupCenterTitle: String
    val exportAllBackup: String
    val exportSelectedBackup: String
    val importAndRestore: String
    val lastBackupDate: String
    val restoreConfirmMessage: String
    val backupSuccess: String
    val restoreSuccess: String
    val backupInvalidError: String

    // Enum Label Helpers
    fun label(role: UserRole): String
    fun label(status: ClientStatus): String
    fun label(type: ClientType): String
    fun label(category: ProjectCategory): String
    fun label(status: ProjectStatus): String
    fun label(priority: PriorityLevel): String
    fun label(status: TaskStatus): String
    fun label(category: ServiceCategory): String
    fun label(priceType: ServicePriceType): String
    fun label(stage: OpportunityStage): String
    fun label(type: TransactionType): String
    fun label(category: TransactionCategory): String
    fun label(status: TransactionStatus): String
    fun label(method: PaymentMethod): String
    fun label(status: InvoiceStatus): String
    fun label(channel: SocialChannel): String
    fun label(status: ContentStatus): String
    fun label(status: CampaignStatus): String
    fun label(category: DocumentCategory): String
    fun label(category: KnowledgeCategory): String
}
