package com.example.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.domain.logic.AnalyticsEngine
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import com.example.ui.navigation.AppRoutes
import com.example.ui.theme.*
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun DashboardScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onNavigate: (String) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var showCombinedEstimate by remember { mutableStateOf(false) }
    var chartCurrency by remember { mutableStateOf(state.settings.defaultCurrency) }

    val activeClients = remember(state.clients) { state.clients.filter { !it.isArchived } }
    val activeProjects = remember(state.projects) { state.projects.filter { !it.isArchived } }
    val activeProjectsCount = remember(activeProjects) {
        activeProjects.count {
            it.status == ProjectStatus.IN_PROGRESS ||
                it.status == ProjectStatus.PLANNING ||
                it.status == ProjectStatus.UNDER_REVIEW ||
                it.status == ProjectStatus.REVISION
        }
    }
    val completedProjectsCount = remember(activeProjects) {
        activeProjects.count { it.status == ProjectStatus.COMPLETED }
    }
    val pendingProjectsCount = remember(activeProjects) {
        activeProjects.count {
            it.status == ProjectStatus.IDEA ||
                it.status == ProjectStatus.NEW_REQUEST ||
                it.status == ProjectStatus.WAITING_FOR_CLIENT
        }
    }
    val now = System.currentTimeMillis()
    val overdueTasks = remember(state.tasks, now) {
        state.tasks.filter {
            it.status != TaskStatus.COMPLETED &&
                it.status != TaskStatus.CANCELLED &&
                it.dueDate != null && it.dueDate < now
        }
    }
    val todaysTasks = remember(state.tasks, now) {
        val (dayStart, dayEnd) = FinancialCalculator.boundsFor(DateRangePreset.TODAY, nowMillis = now)
        state.tasks.filter {
            it.status != TaskStatus.COMPLETED &&
                it.status != TaskStatus.CANCELLED &&
                it.dueDate != null && it.dueDate in dayStart..dayEnd
        }
    }
    val overdueInvoices = remember(state.invoices, now) {
        state.invoices.filter {
            (it.status == InvoiceStatus.OVERDUE ||
                ((it.status == InvoiceStatus.SENT || it.status == InvoiceStatus.PARTIALLY_PAID) && it.dueDate < now)) &&
                (it.total - it.paidAmount) > 0.01
        }
    }

    val summary = state.financialSummary
    val rate = state.settings.usdToIqdRate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Optional Sample Data Banner if user has no real client/financial records yet
        if (activeClients.isEmpty() && state.transactions.isEmpty() && !state.settings.sampleDataActive) {
            item {
                BasluneCard(
                    containerColor = Ivory,
                    borderColor = ChampagneGold
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = RichBrown)
                        Text(
                            text = strings.sampleDataBannerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.sampleDataBannerDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.activateSampleData() },
                        modifier = Modifier.testTag("activate_sample_data_btn")
                    ) {
                        Text(strings.activateSampleData)
                    }
                }
            }
        } else if (state.settings.sampleDataActive) {
            item {
                BasluneCard(
                    containerColor = WarningContainer,
                    borderColor = WarningAmber
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.sampleBadge,
                                style = MaterialTheme.typography.labelLarge,
                                color = DarkText
                            )
                            Text(
                                text = strings.sampleDataBannerDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.deactivateSampleData() }) {
                            Text(strings.deactivateSampleData)
                        }
                    }
                }
            }
        }

        // Date Range Filter Chips
        item {
            var showCustomRangeDialog by remember { mutableStateOf(false) }
            FilterChipRow(
                items = listOf(
                    DateRangePreset.TODAY,
                    DateRangePreset.THIS_WEEK,
                    DateRangePreset.THIS_MONTH,
                    DateRangePreset.THIS_QUARTER,
                    DateRangePreset.THIS_YEAR,
                    DateRangePreset.ALL_TIME,
                    DateRangePreset.CUSTOM
                ),
                selected = state.dateRangePreset,
                labelOf = { preset ->
                    when (preset) {
                        DateRangePreset.TODAY -> strings.dateToday
                        DateRangePreset.THIS_WEEK -> strings.dateThisWeek
                        DateRangePreset.THIS_MONTH -> strings.dateThisMonth
                        DateRangePreset.THIS_QUARTER -> strings.dateThisQuarter
                        DateRangePreset.THIS_YEAR -> strings.dateThisYear
                        DateRangePreset.ALL_TIME -> strings.dateAllTime
                        DateRangePreset.CUSTOM -> strings.dateCustom
                    }
                },
                onSelect = { preset ->
                    if (preset == DateRangePreset.CUSTOM) {
                        showCustomRangeDialog = true
                    } else {
                        viewModel.setDateRangePreset(preset)
                    }
                }
            )
            if (showCustomRangeDialog) {
                var startStr by remember { mutableStateOf(state.customStartMillis?.let { Formatters.formatDate(it) } ?: "") }
                var endStr by remember { mutableStateOf(state.customEndMillis?.let { Formatters.formatDate(it) } ?: "") }
                AlertDialog(
                    onDismissRequest = { showCustomRangeDialog = false },
                    title = { Text(strings.dateCustom) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startStr,
                                onValueChange = { startStr = it },
                                label = { Text("${strings.startDate} (YYYY-MM-DD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = endStr,
                                onValueChange = { endStr = it },
                                label = { Text("${strings.dueDate} (YYYY-MM-DD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val sMillis = Formatters.parseDateOrNull(startStr)
                            val eMillis = Formatters.parseDateOrNull(endStr)?.let { it + 86_399_999L }
                            viewModel.setDateRangePreset(DateRangePreset.CUSTOM, sMillis, eMillis)
                            showCustomRangeDialog = false
                        }) { Text(strings.confirm) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomRangeDialog = false }) { Text(strings.cancel) }
                    }
                )
            }
        }

        // Executive Luxury Financial Hero Card (IQD & USD separated!)
        if (state.canAccess(AppModule.FINANCE)) {
            item {
                BasluneDarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.financialOverview,
                                style = MaterialTheme.typography.titleMedium,
                                color = ChampagneGold
                            )
                            Text(
                                text = strings.separateCurrenciesNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = Ivory.copy(alpha = 0.68f)
                            )
                        }
                        TextButton(onClick = { showCombinedEstimate = !showCombinedEstimate }) {
                            Text(
                                text = if (showCombinedEstimate) "IQD / USD" else strings.showCombinedEstimate,
                                color = ChampagneGold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Net Profit Row
                    Text(
                        text = strings.netProfit,
                        style = MaterialTheme.typography.labelMedium,
                        color = Ivory.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (showCombinedEstimate) {
                        Text(
                            text = Formatters.formatMoney(summary.netProfit.combinedInIqd(rate), CurrencyCode.IQD, lang),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ivory
                        )
                        Text(
                            text = "${strings.manualRateNote}: 1 USD = ${rate.toInt()} IQD",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChampagneGold
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = Formatters.formatMoney(summary.netProfit.iqd, CurrencyCode.IQD, lang),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Ivory
                            )
                            Text(
                                text = Formatters.formatMoney(summary.netProfit.usd, CurrencyCode.USD, lang),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ChampagneGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = RichBrown)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.totalIncome, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(
                                Formatters.formatMoney(summary.income.iqd, CurrencyCode.IQD, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ivory,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                Formatters.formatMoney(summary.income.usd, CurrencyCode.USD, lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = ChampagneGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.totalExpenses, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(
                                Formatters.formatMoney(summary.expenses.iqd, CurrencyCode.IQD, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ivory,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                Formatters.formatMoney(summary.expenses.usd, CurrencyCode.USD, lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.outstandingPayments, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(
                                Formatters.formatMoney(summary.outstandingReceivables.iqd, CurrencyCode.IQD, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ivory,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                Formatters.formatMoney(summary.outstandingReceivables.usd, CurrencyCode.USD, lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningContainer
                            )
                        }
                    }

                    if (summary.revenueGrowthPercent != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val g = summary.revenueGrowthPercent
                        StatusBadge(
                            text = "${strings.revenueGrowth}: ${if (g >= 0) "+" else ""}${String.format("%.1f", g)}%",
                            tone = if (g >= 0) BadgeTone.SUCCESS else BadgeTone.WARNING
                        )
                    }
                }
            }
        }

        // Business Overview KPI Grid
        item {
            SectionHeader(title = strings.businessOverview)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = strings.totalClients,
                        primaryValue = activeClients.size.toString(),
                        secondaryValue = "${strings.activeProjects}: $activeProjectsCount",
                        icon = Icons.Outlined.People,
                        tone = BadgeTone.GOLD,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoutes.CLIENTS) }
                    )
                    KpiStatCard(
                        title = strings.activeProjects,
                        primaryValue = activeProjectsCount.toString(),
                        secondaryValue = "${strings.completedProjects}: $completedProjectsCount",
                        icon = Icons.Outlined.WorkOutline,
                        tone = BadgeTone.SUCCESS,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoutes.PROJECTS) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = strings.pendingProjects,
                        primaryValue = pendingProjectsCount.toString(),
                        icon = Icons.Outlined.HourglassEmpty,
                        tone = BadgeTone.WARNING,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoutes.PROJECTS) }
                    )
                    KpiStatCard(
                        title = strings.overdueTasks,
                        primaryValue = overdueTasks.size.toString(),
                        secondaryValue = "${strings.todaysTasks}: ${todaysTasks.size}",
                        icon = Icons.Outlined.WarningAmber,
                        tone = if (overdueTasks.isNotEmpty()) BadgeTone.ERROR else BadgeTone.SUCCESS,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoutes.TASKS) }
                    )
                }
            }
        }

        // Charts Section
        item {
            SectionHeader(
                title = strings.chartsOverview,
                actionLabel = chartCurrency.name,
                onActionClick = {
                    chartCurrency = if (chartCurrency == CurrencyCode.IQD) CurrencyCode.USD else CurrencyCode.IQD
                }
            )
        }
        if (state.canAccess(AppModule.FINANCE)) {
            item {
                val monthly = remember(state.transactions, chartCurrency) {
                    FinancialCalculator.monthlySeries(state.transactions, chartCurrency, months = 6)
                }
                BasluneCard {
                    Text(
                        text = "${strings.monthlyIncomeReport} vs ${strings.totalExpenses} (${chartCurrency.name})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (monthly.all { it.income == 0.0 && it.expense == 0.0 }) {
                        Text(
                            text = strings.noDataForRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        BasluneDualBarChart(
                            data = monthly.map {
                                BarChartPoint(it.label, it.income.toFloat(), it.expense.toFloat())
                            }
                        )
                    }
                }
            }
        }

        if (activeProjects.isNotEmpty()) {
            item {
                val counts = remember(activeProjects) { AnalyticsEngine.projectStatusCounts(activeProjects) }
                val slices = listOf(
                    DonutSlice(strings.label(ProjectStatus.IN_PROGRESS), (counts[ProjectStatus.IN_PROGRESS] ?: 0).toFloat(), SuccessGreen),
                    DonutSlice(strings.label(ProjectStatus.PLANNING), (counts[ProjectStatus.PLANNING] ?: 0).toFloat(), WarningAmber),
                    DonutSlice(strings.label(ProjectStatus.COMPLETED), (counts[ProjectStatus.COMPLETED] ?: 0).toFloat(), DeepEspresso),
                    DonutSlice(strings.label(ProjectStatus.UNDER_REVIEW), (counts[ProjectStatus.UNDER_REVIEW] ?: 0).toFloat(), MutedTaupe),
                    DonutSlice(strings.label(ProjectStatus.WAITING_FOR_CLIENT), (counts[ProjectStatus.WAITING_FOR_CLIENT] ?: 0).toFloat(), ErrorRed)
                )
                if (slices.any { it.value > 0f }) {
                    BasluneCard {
                        Text(strings.projectReport, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        BasluneDonutChart(slices = slices)
                    }
                }
            }
        }

        // Action Center
        item {
            SectionHeader(title = strings.actionCenter)
        }

        item {
            BasluneCard {
                Text(strings.todaysTasks, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (todaysTasks.isEmpty() && overdueTasks.isEmpty()) {
                    Text(
                        text = strings.emptyTasksTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    (overdueTasks.take(3) + todaysTasks.take(3)).distinctBy { it.id }.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = task.status == TaskStatus.COMPLETED,
                                    onCheckedChange = { viewModel.toggleTaskCompleted(task, it) }
                                )
                                Column {
                                    Text(task.title, style = MaterialTheme.typography.bodyMedium)
                                    task.dueDate?.let {
                                        Text(
                                            Formatters.formatDate(it),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (it < now) ErrorRed else SecondaryText
                                        )
                                    }
                                }
                            }
                            if (task.dueDate != null && task.dueDate < now) {
                                StatusBadge(strings.overdueWarning, BadgeTone.ERROR)
                            }
                        }
                    }
                }
            }
        }

        if (overdueInvoices.isNotEmpty() && state.canAccess(AppModule.INVOICES)) {
            item {
                BasluneCard(borderColor = ErrorRed.copy(alpha = 0.5f)) {
                    Text(strings.overdueInvoices, style = MaterialTheme.typography.titleMedium, color = ErrorRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    overdueInvoices.take(4).forEach { inv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${inv.invoiceNumber} • ${inv.clientNameSnapshot}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                Formatters.formatMoney(inv.total - inv.paidAmount, inv.currency, lang),
                                style = MaterialTheme.typography.labelLarge,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }
        }

        // Recently Updated Projects
        if (activeProjects.isNotEmpty()) {
            item {
                SectionHeader(
                    title = strings.recentProjects,
                    actionLabel = strings.all,
                    onActionClick = { onNavigate(AppRoutes.PROJECTS) }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeProjects.take(4).forEach { project ->
                        BasluneCard(onClick = { onNavigate(AppRoutes.projectDetail(project.id)) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        strings.label(project.category),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SampleOrStarterBadge(project.isSample, project.isStarter)
                                    StatusBadge(
                                        text = strings.label(project.status),
                                        tone = when (project.status) {
                                            ProjectStatus.COMPLETED -> BadgeTone.SUCCESS
                                            ProjectStatus.IN_PROGRESS -> BadgeTone.GOLD
                                            ProjectStatus.WAITING_FOR_CLIENT -> BadgeTone.WARNING
                                            else -> BadgeTone.NEUTRAL
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
