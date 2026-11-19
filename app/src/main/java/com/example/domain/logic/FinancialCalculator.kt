package com.example.domain.logic

import com.example.data.local.InvoiceEntity
import com.example.data.local.TransactionEntity
import com.example.domain.model.*
import java.util.Calendar
import kotlin.math.max

object FinancialCalculator {

    fun filterByDateRange(
        timestamp: Long,
        preset: DateRangePreset,
        customStart: Long? = null,
        customEnd: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (preset == DateRangePreset.ALL_TIME) return true
        val (start, end) = boundsFor(preset, customStart, customEnd, nowMillis)
        return timestamp in start..end
    }

    fun boundsFor(
        preset: DateRangePreset,
        customStart: Long? = null,
        customEnd: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return when (preset) {
            DateRangePreset.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                start to cal.timeInMillis
            }
            DateRangePreset.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                start to nowMillis
            }
            DateRangePreset.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                start to nowMillis
            }
            DateRangePreset.THIS_QUARTER -> {
                val month = cal.get(Calendar.MONTH)
                val quarterStartMonth = (month / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                start to nowMillis
            }
            DateRangePreset.THIS_YEAR -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                start to nowMillis
            }
            DateRangePreset.ALL_TIME -> 0L to Long.MAX_VALUE
            DateRangePreset.CUSTOM -> (customStart ?: 0L) to (customEnd ?: Long.MAX_VALUE)
        }
    }

    fun calculateSummary(
        transactions: List<TransactionEntity>,
        invoices: List<InvoiceEntity>,
        preset: DateRangePreset = DateRangePreset.ALL_TIME,
        customStart: Long? = null,
        customEnd: Long? = null,
        usdToIqdRate: Double = 1500.0,
        nowMillis: Long = System.currentTimeMillis()
    ): FinancialSummary {
        val activeTx = transactions.filter {
            it.status == TransactionStatus.COMPLETED &&
                filterByDateRange(it.date, preset, customStart, customEnd, nowMillis)
        }

        val incomeIqd = activeTx
            .filter { it.type == TransactionType.INCOME && it.currency == CurrencyCode.IQD }
            .sumOf { it.amount }
        val incomeUsd = activeTx
            .filter { it.type == TransactionType.INCOME && it.currency == CurrencyCode.USD }
            .sumOf { it.amount }

        val expenseIqd = activeTx
            .filter { it.type == TransactionType.EXPENSE && it.currency == CurrencyCode.IQD }
            .sumOf { it.amount }
        val expenseUsd = activeTx
            .filter { it.type == TransactionType.EXPENSE && it.currency == CurrencyCode.USD }
            .sumOf { it.amount }

        val filteredInvoices = invoices.filter {
            it.status != InvoiceStatus.DRAFT &&
                it.status != InvoiceStatus.CANCELLED &&
                filterByDateRange(it.issueDate, preset, customStart, customEnd, nowMillis)
        }

        val invoicedIqd = filteredInvoices.filter { it.currency == CurrencyCode.IQD }.sumOf { it.total }
        val invoicedUsd = filteredInvoices.filter { it.currency == CurrencyCode.USD }.sumOf { it.total }

        val activeReceivableInvoices = invoices.filter {
            it.status == InvoiceStatus.SENT ||
                it.status == InvoiceStatus.PARTIALLY_PAID ||
                it.status == InvoiceStatus.OVERDUE
        }

        val outstandingIqd = activeReceivableInvoices
            .filter { it.currency == CurrencyCode.IQD }
            .sumOf { max(0.0, it.total - it.paidAmount) }
        val outstandingUsd = activeReceivableInvoices
            .filter { it.currency == CurrencyCode.USD }
            .sumOf { max(0.0, it.total - it.paidAmount) }

        val paidCount = invoices.count { it.status == InvoiceStatus.PAID }
        val unpaidOrOverdueCount = activeReceivableInvoices.count { max(0.0, it.total - it.paidAmount) > 0.001 }

        // Current month vs previous month revenue (for growth calculation)
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        cal.add(Calendar.MONTH, -1)
        val prevMonth = cal.get(Calendar.MONTH)
        val prevYear = cal.get(Calendar.YEAR)

        val completedIncomeTx = transactions.filter {
            it.status == TransactionStatus.COMPLETED && it.type == TransactionType.INCOME
        }

        var thisMonthIqd = 0.0
        var thisMonthUsd = 0.0
        var lastMonthIqd = 0.0
        var lastMonthUsd = 0.0

        val txCal = Calendar.getInstance()
        completedIncomeTx.forEach { tx ->
            txCal.timeInMillis = tx.date
            val m = txCal.get(Calendar.MONTH)
            val y = txCal.get(Calendar.YEAR)
            if (m == currentMonth && y == currentYear) {
                if (tx.currency == CurrencyCode.IQD) thisMonthIqd += tx.amount else thisMonthUsd += tx.amount
            } else if (m == prevMonth && y == prevYear) {
                if (tx.currency == CurrencyCode.IQD) lastMonthIqd += tx.amount else lastMonthUsd += tx.amount
            }
        }

        val currentCombined = thisMonthIqd + thisMonthUsd * usdToIqdRate
        val prevCombined = lastMonthIqd + lastMonthUsd * usdToIqdRate
        val growthPercent = if (prevCombined > 0.0) {
            ((currentCombined - prevCombined) / prevCombined) * 100.0
        } else null

        return FinancialSummary(
            income = CurrencyBreakdown(iqd = incomeIqd, usd = incomeUsd),
            expenses = CurrencyBreakdown(iqd = expenseIqd, usd = expenseUsd),
            netProfit = CurrencyBreakdown(iqd = incomeIqd - expenseIqd, usd = incomeUsd - expenseUsd),
            outstandingReceivables = CurrencyBreakdown(iqd = outstandingIqd, usd = outstandingUsd),
            totalInvoiced = CurrencyBreakdown(iqd = invoicedIqd, usd = invoicedUsd),
            paidInvoicesCount = paidCount,
            unpaidOrOverdueInvoicesCount = unpaidOrOverdueCount,
            monthlyRevenueIqd = thisMonthIqd,
            monthlyRevenueUsd = thisMonthUsd,
            previousMonthRevenueIqd = lastMonthIqd,
            previousMonthRevenueUsd = lastMonthUsd,
            revenueGrowthPercent = growthPercent
        )
    }

    fun calculateProjectProfitability(
        projectId: Long,
        transactions: List<TransactionEntity>
    ): ProjectProfitability {
        val projectTx = transactions.filter {
            it.projectId == projectId && it.status == TransactionStatus.COMPLETED
        }
        val incIqd = projectTx.filter { it.type == TransactionType.INCOME && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
        val incUsd = projectTx.filter { it.type == TransactionType.INCOME && it.currency == CurrencyCode.USD }.sumOf { it.amount }
        val expIqd = projectTx.filter { it.type == TransactionType.EXPENSE && it.currency == CurrencyCode.IQD }.sumOf { it.amount }
        val expUsd = projectTx.filter { it.type == TransactionType.EXPENSE && it.currency == CurrencyCode.USD }.sumOf { it.amount }
        return ProjectProfitability(
            projectId = projectId,
            incomeIqd = incIqd,
            incomeUsd = incUsd,
            expenseIqd = expIqd,
            expenseUsd = expUsd,
            profitIqd = incIqd - expIqd,
            profitUsd = incUsd - expUsd
        )
    }

    fun computeInvoiceTotals(
        itemTotals: List<Double>,
        discount: Double,
        tax: Double
    ): Triple<Double, Double, Double> {
        val subtotal = itemTotals.sum().coerceAtLeast(0.0)
        val clampedDiscount = discount.coerceIn(0.0, subtotal)
        val validTax = tax.coerceAtLeast(0.0)
        val total = (subtotal - clampedDiscount + validTax).coerceAtLeast(0.0)
        return Triple(subtotal, clampedDiscount, total)
    }

    fun resolveInvoiceStatus(
        currentStatus: InvoiceStatus,
        total: Double,
        paidAmount: Double,
        dueDate: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): InvoiceStatus {
        if (currentStatus == InvoiceStatus.CANCELLED) return InvoiceStatus.CANCELLED
        if (total > 0.0 && paidAmount >= total - 0.01) return InvoiceStatus.PAID
        if (paidAmount > 0.01) return InvoiceStatus.PARTIALLY_PAID
        if (currentStatus == InvoiceStatus.DRAFT) return InvoiceStatus.DRAFT
        return if (dueDate < nowMillis) InvoiceStatus.OVERDUE else InvoiceStatus.SENT
    }

    data class MonthlySeriesPoint(
        val label: String,
        val income: Double,
        val expense: Double
    )

    fun monthlySeries(
        transactions: List<TransactionEntity>,
        currency: CurrencyCode,
        months: Int = 6,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MonthlySeriesPoint> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, -(months - 1))

        val points = mutableListOf<MonthlySeriesPoint>()
        val txCal = Calendar.getInstance()
        val active = transactions.filter {
            it.status == TransactionStatus.COMPLETED && it.currency == currency
        }

        repeat(months) {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val label = "${m + 1}/${y % 100}"
            var inc = 0.0
            var exp = 0.0
            active.forEach { tx ->
                txCal.timeInMillis = tx.date
                if (txCal.get(Calendar.YEAR) == y && txCal.get(Calendar.MONTH) == m) {
                    if (tx.type == TransactionType.INCOME) inc += tx.amount else exp += tx.amount
                }
            }
            points.add(MonthlySeriesPoint(label = label, income = inc, expense = exp))
            cal.add(Calendar.MONTH, 1)
        }
        return points
    }
}
