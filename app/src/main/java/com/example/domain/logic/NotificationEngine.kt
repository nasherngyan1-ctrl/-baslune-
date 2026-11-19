package com.example.domain.logic

import com.example.core.localization.AppStrings
import com.example.core.localization.Formatters
import com.example.data.local.*
import com.example.domain.model.*

object NotificationEngine {

    fun generateRealNotifications(
        tasks: List<TaskEntity>,
        projects: List<ProjectEntity>,
        invoices: List<InvoiceEntity>,
        opportunities: List<OpportunityEntity>,
        communications: List<ClientCommunicationEntity>,
        readIds: Set<String>,
        strings: AppStrings,
        language: com.example.core.localization.AppLanguage,
        nowMillis: Long = System.currentTimeMillis()
    ): List<ComputedNotification> {
        val items = mutableListOf<ComputedNotification>()
        val threeDaysLater = nowMillis + 3L * 24 * 60 * 60 * 1000

        // 1. Overdue Tasks
        tasks.filter {
            it.status != TaskStatus.COMPLETED &&
                it.status != TaskStatus.CANCELLED &&
                it.dueDate != null &&
                it.dueDate < nowMillis
        }.forEach { task ->
            val id = "task_overdue_${task.id}"
            items += ComputedNotification(
                id = id,
                type = NotificationType.OVERDUE_TASK,
                title = strings.overdueTasks,
                message = "${task.title} (${Formatters.formatDate(task.dueDate!!)})",
                timestamp = task.dueDate,
                isRead = readIds.contains(id),
                targetRoute = "tasks"
            )
        }

        // 2. Upcoming Project Deadlines (within 3 days)
        projects.filter {
            !it.isArchived &&
                it.status != ProjectStatus.COMPLETED &&
                it.status != ProjectStatus.CANCELLED &&
                it.deadline != null &&
                it.deadline in nowMillis..threeDaysLater
        }.forEach { project ->
            val id = "project_deadline_${project.id}"
            items += ComputedNotification(
                id = id,
                type = NotificationType.UPCOMING_DEADLINE,
                title = strings.upcomingDeadlines,
                message = "${project.name} — ${Formatters.formatDate(project.deadline!!)}",
                timestamp = project.deadline,
                isRead = readIds.contains(id),
                targetRoute = "project_detail/${project.id}"
            )
        }

        // 3. Unpaid / Overdue Invoices
        invoices.filter {
            (it.status == InvoiceStatus.SENT ||
                it.status == InvoiceStatus.PARTIALLY_PAID ||
                it.status == InvoiceStatus.OVERDUE) &&
                (it.total - it.paidAmount) > 0.01
        }.forEach { inv ->
            val isOverdue = inv.dueDate < nowMillis || inv.status == InvoiceStatus.OVERDUE
            val id = "invoice_${if (isOverdue) "overdue" else "unpaid"}_${inv.id}"
            val remaining = Formatters.formatMoney(inv.total - inv.paidAmount, inv.currency, language)
            items += ComputedNotification(
                id = id,
                type = NotificationType.UNPAID_INVOICE,
                title = if (isOverdue) strings.overdueInvoices else strings.outstandingPayments,
                message = "${inv.invoiceNumber} • ${inv.clientNameSnapshot} • $remaining",
                timestamp = inv.dueDate,
                isRead = readIds.contains(id),
                targetRoute = "invoice_detail/${inv.id}"
            )
        }

        // 4. Sales Follow-ups due
        opportunities.filter {
            it.stage != OpportunityStage.WON &&
                it.stage != OpportunityStage.LOST &&
                it.nextFollowUpDate != null &&
                it.nextFollowUpDate <= threeDaysLater
        }.forEach { opp ->
            val id = "opp_followup_${opp.id}"
            items += ComputedNotification(
                id = id,
                type = NotificationType.FOLLOW_UP_DUE,
                title = strings.followUpDate,
                message = "${opp.title} (${opp.potentialClientName}) — ${Formatters.formatDate(opp.nextFollowUpDate!!)}",
                timestamp = opp.nextFollowUpDate,
                isRead = readIds.contains(id),
                targetRoute = "sales"
            )
        }

        // 5. Client Communication Follow-ups due
        communications.filter {
            it.followUpDate != null && it.followUpDate <= threeDaysLater
        }.forEach { comm ->
            val id = "comm_followup_${comm.id}"
            items += ComputedNotification(
                id = id,
                type = NotificationType.FOLLOW_UP_DUE,
                title = strings.followUpDate,
                message = "${comm.interactionType}: ${comm.summary} (${Formatters.formatDate(comm.followUpDate!!)})",
                timestamp = comm.followUpDate,
                isRead = readIds.contains(id),
                targetRoute = "client_detail/${comm.clientId}"
            )
        }

        return items.sortedWith(compareBy<ComputedNotification> { it.isRead }.thenByDescending { it.timestamp })
    }
}
