package com.example.ui.screens.reports

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.data.repository.BackupManager
import com.example.domain.logic.AnalyticsEngine
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

enum class ReportType {
    FINANCIAL, MONTHLY_INCOME, EXPENSE, PROFIT, CLIENT, PROJECT, SERVICE, SALES, TASK, MARKETING
}

@Composable
fun ReportsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    val availableReports = remember(state.settings.activeRole) {
        if (state.canAccess(AppModule.FINANCE)) {
            ReportType.entries
        } else {
            listOf(ReportType.CLIENT, ReportType.PROJECT, ReportType.SERVICE, ReportType.SALES, ReportType.TASK, ReportType.MARKETING)
        }
    }
    var selectedReport by remember(availableReports) { mutableStateOf(availableReports.first()) }
    var datePreset by remember { mutableStateOf(DateRangePreset.ALL_TIME) }
    var pendingCsv by remember { mutableStateOf("") }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null && pendingCsv.isNotEmpty()) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(pendingCsv.toByteArray(Charsets.UTF_8))
                }
                viewModel.showMessage(strings.copiedOrSaved)
            }.onFailure {
                viewModel.showMessage(it.message ?: "Export failed")
            }
        }
    }

    val summary = remember(state.transactions, state.invoices, datePreset, state.settings.usdToIqdRate) {
        FinancialCalculator.calculateSummary(
            transactions = state.transactions,
            invoices = state.invoices,
            preset = datePreset,
            usdToIqdRate = state.settings.usdToIqdRate
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FilterChipRow(
                items = listOf(
                    DateRangePreset.TODAY,
                    DateRangePreset.THIS_WEEK,
                    DateRangePreset.THIS_MONTH,
                    DateRangePreset.THIS_QUARTER,
                    DateRangePreset.THIS_YEAR,
                    DateRangePreset.ALL_TIME
                ),
                selected = datePreset,
                labelOf = { p ->
                    when (p) {
                        DateRangePreset.TODAY -> strings.dateToday
                        DateRangePreset.THIS_WEEK -> strings.dateThisWeek
                        DateRangePreset.THIS_MONTH -> strings.dateThisMonth
                        DateRangePreset.THIS_QUARTER -> strings.dateThisQuarter
                        DateRangePreset.THIS_YEAR -> strings.dateThisYear
                        else -> strings.dateAllTime
                    }
                },
                onSelect = { datePreset = it }
            )
        }

        item {
            FilterChipRow(
                items = availableReports,
                selected = selectedReport,
                labelOf = { r ->
                    when (r) {
                        ReportType.FINANCIAL -> strings.financialReport
                        ReportType.MONTHLY_INCOME -> strings.monthlyIncomeReport
                        ReportType.EXPENSE -> strings.expenseReport
                        ReportType.PROFIT -> strings.profitReport
                        ReportType.CLIENT -> strings.clientReport
                        ReportType.PROJECT -> strings.projectReport
                        ReportType.SERVICE -> strings.serviceReport
                        ReportType.SALES -> strings.salesReport
                        ReportType.TASK -> strings.taskReport
                        ReportType.MARKETING -> strings.marketingReport
                    }
                },
                onSelect = { selectedReport = it }
            )
        }

        item {
            Button(
                onClick = {
                    pendingCsv = buildCsvForReport(selectedReport, state, summary)
                    csvLauncher.launch("BASLUNE_${selectedReport.name.lowercase()}_report.csv")
                },
                modifier = Modifier.fillMaxWidth().testTag("export_csv_button")
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.exportCsv)
            }
        }

        item {
            when (selectedReport) {
                ReportType.FINANCIAL, ReportType.PROFIT -> {
                    BasluneCard {
                        Text(strings.financialReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRow(strings.totalIncome, "${Formatters.formatMoney(summary.income.iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(summary.income.usd, CurrencyCode.USD, lang)}")
                        ReportRow(strings.totalExpenses, "${Formatters.formatMoney(summary.expenses.iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(summary.expenses.usd, CurrencyCode.USD, lang)}")
                        ReportRow(strings.netProfit, "${Formatters.formatMoney(summary.netProfit.iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(summary.netProfit.usd, CurrencyCode.USD, lang)}")
                        ReportRow(strings.outstandingPayments, "${Formatters.formatMoney(summary.outstandingReceivables.iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(summary.outstandingReceivables.usd, CurrencyCode.USD, lang)}")
                    }
                }
                ReportType.MONTHLY_INCOME -> {
                    val series = FinancialCalculator.monthlySeries(state.transactions, state.settings.defaultCurrency, 6)
                    BasluneCard {
                        Text("${strings.monthlyIncomeReport} (${state.settings.defaultCurrency.name})", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        BasluneTrendLineChart(points = series.map { it.label to it.income.toFloat() })
                        Spacer(modifier = Modifier.height(8.dp))
                        series.forEach { pt ->
                            ReportRow(pt.label, Formatters.formatMoney(pt.income, state.settings.defaultCurrency, lang))
                        }
                    }
                }
                ReportType.EXPENSE -> {
                    val expenses = state.transactions.filter {
                        it.type == TransactionType.EXPENSE && it.status == TransactionStatus.COMPLETED &&
                            FinancialCalculator.filterByDateRange(it.date, datePreset)
                    }
                    BasluneCard {
                        Text(strings.expenseReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (expenses.isEmpty()) {
                            Text(strings.noDataForRange, style = MaterialTheme.typography.bodySmall)
                        } else {
                            expenses.groupBy { it.category }.forEach { (cat, list) ->
                                val iqd = list.filter { it.currency == CurrencyCode.IQD }.sumOf { it.amount }
                                val usd = list.filter { it.currency == CurrencyCode.USD }.sumOf { it.amount }
                                ReportRow(strings.label(cat), "${Formatters.formatMoney(iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(usd, CurrencyCode.USD, lang)}")
                            }
                        }
                    }
                }
                ReportType.CLIENT -> {
                    BasluneCard {
                        Text(strings.clientReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ClientStatus.entries.forEach { st ->
                            val count = state.clients.count { it.status == st }
                            if (count > 0) ReportRow(strings.label(st), count.toString())
                        }
                    }
                }
                ReportType.PROJECT -> {
                    val counts = AnalyticsEngine.projectStatusCounts(state.projects)
                    BasluneCard {
                        Text(strings.projectReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        counts.forEach { (st, c) ->
                            ReportRow(strings.label(st), c.toString())
                        }
                    }
                }
                ReportType.SERVICE -> {
                    val rows = AnalyticsEngine.servicePerformance(state.services, state.opportunities, state.invoiceItems, state.invoices)
                    BasluneCard {
                        Text(strings.serviceReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (rows.isEmpty()) Text(strings.emptyServicesTitle, style = MaterialTheme.typography.bodySmall)
                        rows.forEach { r ->
                            ReportRow(r.serviceName, "${r.usageCount}× • ${Formatters.formatMoney(r.revenueIqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(r.revenueUsd, CurrencyCode.USD, lang)}")
                        }
                    }
                }
                ReportType.SALES -> {
                    val openVal = AnalyticsEngine.openPipelineValue(state.opportunities)
                    val conv = AnalyticsEngine.pipelineConversionRate(state.opportunities)
                    BasluneCard {
                        Text(strings.salesReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRow(strings.pipelineValue, "${Formatters.formatMoney(openVal.iqd, CurrencyCode.IQD, lang)} | ${Formatters.formatMoney(openVal.usd, CurrencyCode.USD, lang)}")
                        ReportRow(strings.conversionRate, "${conv.toInt()}%")
                        OpportunityStage.entries.forEach { st ->
                            val c = state.opportunities.count { it.stage == st }
                            if (c > 0) ReportRow(strings.label(st), c.toString())
                        }
                    }
                }
                ReportType.TASK -> {
                    val rate = AnalyticsEngine.taskCompletionRate(state.tasks)
                    BasluneCard {
                        Text(strings.taskReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRow(strings.progress, "${rate.toInt()}%")
                        TaskStatus.entries.forEach { st ->
                            val c = state.tasks.count { it.status == st }
                            ReportRow(strings.label(st), c.toString())
                        }
                    }
                }
                ReportType.MARKETING -> {
                    BasluneCard {
                        Text(strings.marketingReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRow(strings.campaignsTab, state.campaigns.size.toString())
                        ReportRow(strings.leadsGenerated, state.campaigns.sumOf { it.leadsGenerated }.toString())
                        ReportRow(strings.contentCalendarTab, state.contentItems.size.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
}

private fun buildCsvForReport(
    type: ReportType,
    state: BasluneUiState,
    summary: FinancialSummary
): String {
    return when (type) {
        ReportType.FINANCIAL, ReportType.PROFIT, ReportType.MONTHLY_INCOME, ReportType.EXPENSE -> {
            BackupManager.exportCsv(
                headers = listOf("Code", "Type", "Category", "Amount", "Currency", "Date", "Description"),
                rows = state.transactions.map {
                    listOf(it.transactionCode, it.type.name, it.category.name, it.amount.toString(), it.currency.name, Formatters.formatDate(it.date), it.description)
                }
            )
        }
        ReportType.CLIENT -> BackupManager.exportCsv(
            headers = listOf("Code", "Name", "Company", "City", "Status", "Phone", "Email"),
            rows = state.clients.map { listOf(it.clientCode, it.fullName, it.companyName, it.city, it.status.name, it.phone, it.email) }
        )
        ReportType.PROJECT -> BackupManager.exportCsv(
            headers = listOf("Code", "Name", "Category", "Status", "Progress", "AgreedPrice", "Currency"),
            rows = state.projects.map { listOf(it.projectCode, it.name, it.category.name, it.status.name, "${it.progressPercent}%", it.agreedPrice.toString(), it.currency.name) }
        )
        ReportType.SERVICE -> BackupManager.exportCsv(
            headers = listOf("Name", "Category", "PriceType", "StartingPrice", "Currency", "Active"),
            rows = state.services.map { listOf(it.name, it.category.name, it.priceType.name, it.startingPrice.toString(), it.currency.name, it.isActive.toString()) }
        )
        ReportType.SALES -> BackupManager.exportCsv(
            headers = listOf("Title", "PotentialClient", "Stage", "Value", "Currency", "Probability"),
            rows = state.opportunities.map { listOf(it.title, it.potentialClientName, it.stage.name, it.estimatedValue.toString(), it.currency.name, "${it.probability}%") }
        )
        ReportType.TASK -> BackupManager.exportCsv(
            headers = listOf("Title", "Priority", "Status", "DueDate", "AssignedTo"),
            rows = state.tasks.map { listOf(it.title, it.priority.name, it.status.name, it.dueDate?.let { d -> Formatters.formatDate(d) } ?: "", it.assignedTo) }
        )
        ReportType.MARKETING -> BackupManager.exportCsv(
            headers = listOf("Campaign", "Channel", "Status", "AdSpend", "Currency", "Leads"),
            rows = state.campaigns.map { listOf(it.name, it.channel.name, it.status.name, it.adSpend.toString(), it.currency.name, it.leadsGenerated.toString()) }
        )
    }
}
