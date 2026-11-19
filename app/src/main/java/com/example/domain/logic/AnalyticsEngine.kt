package com.example.domain.logic

import com.example.data.local.*
import com.example.domain.model.*

object AnalyticsEngine {

    fun projectStatusCounts(projects: List<ProjectEntity>): Map<ProjectStatus, Int> {
        return projects.filter { !it.isArchived }.groupingBy { it.status }.eachCount()
    }

    fun pipelineConversionRate(opportunities: List<OpportunityEntity>): Double {
        val won = opportunities.count { it.stage == OpportunityStage.WON }
        val lost = opportunities.count { it.stage == OpportunityStage.LOST }
        val closed = won + lost
        if (closed == 0) {
            return if (opportunities.isEmpty()) 0.0 else (won.toDouble() / opportunities.size) * 100.0
        }
        return (won.toDouble() / closed) * 100.0
    }

    fun openPipelineValue(opportunities: List<OpportunityEntity>): CurrencyBreakdown {
        val open = opportunities.filter { it.stage != OpportunityStage.LOST && it.stage != OpportunityStage.WON }
        return CurrencyBreakdown(
            iqd = open.filter { it.currency == CurrencyCode.IQD }.sumOf { it.estimatedValue },
            usd = open.filter { it.currency == CurrencyCode.USD }.sumOf { it.estimatedValue }
        )
    }

    fun taskCompletionRate(tasks: List<TaskEntity>): Double {
        val nonCancelled = tasks.filter { it.status != TaskStatus.CANCELLED }
        if (nonCancelled.isEmpty()) return 0.0
        val completed = nonCancelled.count { it.status == TaskStatus.COMPLETED }
        return (completed.toDouble() / nonCancelled.size) * 100.0
    }

    data class ServicePerformanceRow(
        val serviceName: String,
        val usageCount: Int,
        val revenueIqd: Double,
        val revenueUsd: Double
    )

    fun servicePerformance(
        services: List<ServiceEntity>,
        opportunities: List<OpportunityEntity>,
        invoiceItems: List<InvoiceItemEntity>,
        invoices: List<InvoiceEntity>
    ): List<ServicePerformanceRow> {
        val invoiceMap = invoices.associateBy { it.id }
        return services.map { service ->
            val oppCount = opportunities.count { it.requestedServiceId == service.id }
            val matchedItems = invoiceItems.filter { it.serviceId == service.id }
            var revIqd = 0.0
            var revUsd = 0.0
            matchedItems.forEach { item ->
                val inv = invoiceMap[item.invoiceId]
                if (inv != null && inv.status != InvoiceStatus.CANCELLED && inv.status != InvoiceStatus.DRAFT) {
                    if (inv.currency == CurrencyCode.IQD) revIqd += item.lineTotal else revUsd += item.lineTotal
                }
            }
            ServicePerformanceRow(
                serviceName = service.name,
                usageCount = oppCount + matchedItems.size,
                revenueIqd = revIqd,
                revenueUsd = revUsd
            )
        }.sortedByDescending { it.usageCount }
    }
}
