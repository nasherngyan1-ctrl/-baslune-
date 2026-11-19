package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.*

class BasluneTypeConverters {
    @TypeConverter fun fromCurrency(v: CurrencyCode): String = v.name
    @TypeConverter fun toCurrency(v: String): CurrencyCode = CurrencyCode.valueOf(v)

    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = UserRole.valueOf(v)

    @TypeConverter fun fromClientStatus(v: ClientStatus): String = v.name
    @TypeConverter fun toClientStatus(v: String): ClientStatus = ClientStatus.valueOf(v)

    @TypeConverter fun fromClientType(v: ClientType): String = v.name
    @TypeConverter fun toClientType(v: String): ClientType = ClientType.valueOf(v)

    @TypeConverter fun fromProjectCategory(v: ProjectCategory): String = v.name
    @TypeConverter fun toProjectCategory(v: String): ProjectCategory = ProjectCategory.valueOf(v)

    @TypeConverter fun fromProjectStatus(v: ProjectStatus): String = v.name
    @TypeConverter fun toProjectStatus(v: String): ProjectStatus = ProjectStatus.valueOf(v)

    @TypeConverter fun fromPriority(v: PriorityLevel): String = v.name
    @TypeConverter fun toPriority(v: String): PriorityLevel = PriorityLevel.valueOf(v)

    @TypeConverter fun fromTaskStatus(v: TaskStatus): String = v.name
    @TypeConverter fun toTaskStatus(v: String): TaskStatus = TaskStatus.valueOf(v)

    @TypeConverter fun fromServiceCategory(v: ServiceCategory): String = v.name
    @TypeConverter fun toServiceCategory(v: String): ServiceCategory = ServiceCategory.valueOf(v)

    @TypeConverter fun fromServicePriceType(v: ServicePriceType): String = v.name
    @TypeConverter fun toServicePriceType(v: String): ServicePriceType = ServicePriceType.valueOf(v)

    @TypeConverter fun fromOpportunityStage(v: OpportunityStage): String = v.name
    @TypeConverter fun toOpportunityStage(v: String): OpportunityStage = OpportunityStage.valueOf(v)

    @TypeConverter fun fromTransactionType(v: TransactionType): String = v.name
    @TypeConverter fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)

    @TypeConverter fun fromTransactionCategory(v: TransactionCategory): String = v.name
    @TypeConverter fun toTransactionCategory(v: String): TransactionCategory = TransactionCategory.valueOf(v)

    @TypeConverter fun fromTransactionStatus(v: TransactionStatus): String = v.name
    @TypeConverter fun toTransactionStatus(v: String): TransactionStatus = TransactionStatus.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod): String = v.name
    @TypeConverter fun toPaymentMethod(v: String): PaymentMethod = PaymentMethod.valueOf(v)

    @TypeConverter fun fromInvoiceStatus(v: InvoiceStatus): String = v.name
    @TypeConverter fun toInvoiceStatus(v: String): InvoiceStatus = InvoiceStatus.valueOf(v)

    @TypeConverter fun fromSocialChannel(v: SocialChannel): String = v.name
    @TypeConverter fun toSocialChannel(v: String): SocialChannel = SocialChannel.valueOf(v)

    @TypeConverter fun fromContentStatus(v: ContentStatus): String = v.name
    @TypeConverter fun toContentStatus(v: String): ContentStatus = ContentStatus.valueOf(v)

    @TypeConverter fun fromCampaignStatus(v: CampaignStatus): String = v.name
    @TypeConverter fun toCampaignStatus(v: String): CampaignStatus = CampaignStatus.valueOf(v)

    @TypeConverter fun fromDocumentCategory(v: DocumentCategory): String = v.name
    @TypeConverter fun toDocumentCategory(v: String): DocumentCategory = DocumentCategory.valueOf(v)

    @TypeConverter fun fromKnowledgeCategory(v: KnowledgeCategory): String = v.name
    @TypeConverter fun toKnowledgeCategory(v: String): KnowledgeCategory = KnowledgeCategory.valueOf(v)
}
