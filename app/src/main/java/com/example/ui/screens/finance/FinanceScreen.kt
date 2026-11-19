package com.example.ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.security.InputValidator
import com.example.data.local.TransactionEntity
import com.example.domain.model.*
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Ivory
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun FinanceScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    quickActionType: TransactionType? = null,
    onConsumeQuickAction: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var typeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var currencyFilter by remember { mutableStateOf<CurrencyCode?>(null) }
    var showFormType by remember { mutableStateOf<TransactionType?>(null) }
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<TransactionEntity?>(null) }
    var rateInput by remember(state.settings.usdToIqdRate) {
        mutableStateOf(state.settings.usdToIqdRate.toInt().toString())
    }
    var showAuditLogs by remember { mutableStateOf(false) }

    LaunchedEffect(quickActionType) {
        if (quickActionType != null) {
            editingTx = null
            showFormType = quickActionType
            onConsumeQuickAction()
        }
    }

    val filtered = remember(state.transactions, typeFilter, currencyFilter) {
        state.transactions.filter { tx ->
            (typeFilter == null || tx.type == typeFilter) &&
                (currencyFilter == null || tx.currency == currencyFilter)
        }
    }

    val summary = state.financialSummary

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatingActionButton(
                    onClick = {
                        editingTx = null
                        showFormType = TransactionType.EXPENSE
                    },
                    containerColor = ErrorRed,
                    contentColor = Ivory,
                    modifier = Modifier.testTag("record_expense_fab")
                ) {
                    Icon(Icons.Outlined.ArrowUpward, contentDescription = strings.recordExpense)
                }
                FloatingActionButton(
                    onClick = {
                        editingTx = null
                        showFormType = TransactionType.INCOME
                    },
                    modifier = Modifier.testTag("record_income_fab")
                ) {
                    Icon(Icons.Outlined.ArrowDownward, contentDescription = strings.recordIncome)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            item {
                BasluneDarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Text(strings.financialOverview, style = MaterialTheme.typography.titleMedium, color = ChampagneGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        strings.separateCurrenciesNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = Ivory.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(strings.totalIncome, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(Formatters.formatMoney(summary.income.iqd, CurrencyCode.IQD, lang), color = Ivory, fontWeight = FontWeight.Bold)
                            Text(Formatters.formatMoney(summary.income.usd, CurrencyCode.USD, lang), color = ChampagneGold)
                        }
                        Column {
                            Text(strings.totalExpenses, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(Formatters.formatMoney(summary.expenses.iqd, CurrencyCode.IQD, lang), color = Ivory, fontWeight = FontWeight.Bold)
                            Text(Formatters.formatMoney(summary.expenses.usd, CurrencyCode.USD, lang), color = ChampagneGold)
                        }
                        Column {
                            Text(strings.netProfit, style = MaterialTheme.typography.labelSmall, color = Ivory.copy(alpha = 0.7f))
                            Text(Formatters.formatMoney(summary.netProfit.iqd, CurrencyCode.IQD, lang), color = Ivory, fontWeight = FontWeight.Bold)
                            Text(Formatters.formatMoney(summary.netProfit.usd, CurrencyCode.USD, lang), color = ChampagneGold)
                        }
                    }
                }
            }

            // Manual Exchange Rate Card
            item {
                BasluneCard {
                    Text(strings.exchangeRateTitle, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = { rateInput = it },
                            label = { Text(strings.usdToIqdRate) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val r = rateInput.toDoubleOrNull()
                                if (r != null && r > 0) {
                                    viewModel.setExchangeRate(r)
                                    viewModel.showMessage(strings.copiedOrSaved)
                                }
                            }
                        ) {
                            Text(strings.updateRate)
                        }
                    }
                }
            }

            // Filters + Audit History toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterChipRow(
                            items = listOf(null, TransactionType.INCOME, TransactionType.EXPENSE),
                            selected = typeFilter,
                            labelOf = { it?.let { t -> strings.label(t) } ?: strings.all },
                            onSelect = { typeFilter = it }
                        )
                    }
                    TextButton(onClick = { showAuditLogs = !showAuditLogs }) {
                        Text(strings.auditHistory)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                FilterChipRow(
                    items = listOf(null) + CurrencyCode.entries,
                    selected = currencyFilter,
                    labelOf = { it?.name ?: strings.all },
                    onSelect = { currencyFilter = it }
                )
            }

            if (showAuditLogs) {
                val financeLogs = state.activityLogs.filter { it.entityType == "FINANCE" || it.entityType == "PAYMENT" }
                item {
                    BasluneCard {
                        Text(strings.auditHistory, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (financeLogs.isEmpty()) {
                            Text("—", style = MaterialTheme.typography.bodySmall)
                        } else {
                            financeLogs.take(12).forEach { log ->
                                Text(
                                    "${Formatters.formatDateTime(log.timestamp)} • ${log.action} • ${log.details}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = strings.emptyFinanceTitle,
                        description = strings.emptyFinanceDesc,
                        actionLabel = strings.recordIncome,
                        onAction = {
                            editingTx = null
                            showFormType = TransactionType.INCOME
                        }
                    )
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
                    BasluneCard(
                        onClick = {
                            editingTx = tx
                            showFormType = tx.type
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tx.description.ifBlank { strings.label(tx.category) },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${Formatters.formatDate(tx.date)} • ${strings.label(tx.category)} • ${strings.label(tx.paymentMethod)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
                                Text(
                                    text = "$sign${Formatters.formatMoney(tx.amount, tx.currency, lang)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == TransactionType.INCOME) SuccessGreen else ErrorRed
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    SampleOrStarterBadge(tx.isSample)
                                    IconButton(
                                        onClick = { deleteCandidate = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = strings.delete, tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormType != null) {
        TransactionFormDialog(
            initial = editingTx,
            type = showFormType!!,
            state = state,
            onDismiss = { showFormType = null },
            onSave = {
                viewModel.saveTransaction(it)
                showFormType = null
            }
        )
    }

    deleteCandidate?.let { tx ->
        ConfirmDestructiveDialog(
            title = strings.delete,
            message = strings.deleteTransactionConfirm,
            confirmLabel = strings.delete,
            onConfirm = {
                viewModel.deleteTransaction(tx)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }
}

@Composable
private fun TransactionFormDialog(
    initial: TransactionEntity?,
    type: TransactionType,
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val strings = LocalStrings.current
    val categories = remember(type) {
        TransactionCategory.entries.filter {
            if (type == TransactionType.INCOME) it.isIncome() else !it.isIncome()
        }
    }
    var amountStr by remember { mutableStateOf(if ((initial?.amount ?: 0.0) > 0) initial!!.amount.toString() else "") }
    var currency by remember { mutableStateOf(initial?.currency ?: state.settings.defaultCurrency) }
    var category by remember { mutableStateOf(initial?.category ?: categories.first()) }
    var method by remember { mutableStateOf(initial?.paymentMethod ?: PaymentMethod.CASH) }
    var dateStr by remember { mutableStateOf(Formatters.formatDate(initial?.date ?: System.currentTimeMillis())) }
    var clientId by remember { mutableStateOf(initial?.clientId) }
    var projectId by remember { mutableStateOf(initial?.projectId) }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var receiptUrl by remember { mutableStateOf(initial?.receiptUrl ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val activeClients = state.clients.filter { !it.isArchived }
    val activeProjects = state.projects.filter { !it.isArchived }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    initial != null -> strings.editTransaction
                    type == TransactionType.INCOME -> strings.recordIncome
                    else -> strings.recordExpense
                }
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("${strings.amount} *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("tx_amount_input")
                        )
                        FilterChipRow(
                            items = CurrencyCode.entries,
                            selected = currency,
                            labelOf = { it.name },
                            onSelect = { currency = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(strings.category, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = categories,
                        selected = category,
                        labelOf = { strings.label(it) },
                        onSelect = { category = it }
                    )
                }
                item {
                    Text(strings.paymentMethod, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = PaymentMethod.entries,
                        selected = method,
                        labelOf = { strings.label(it) },
                        onSelect = { method = it }
                    )
                }
                item {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("${strings.date} (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (activeClients.isNotEmpty()) {
                    item {
                        Text(strings.client, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + activeClients.map { it.id },
                            selected = clientId,
                            labelOf = { id ->
                                id?.let { cid -> activeClients.firstOrNull { it.id == cid }?.fullName } ?: strings.none
                            },
                            onSelect = { clientId = it }
                        )
                    }
                }
                if (activeProjects.isNotEmpty()) {
                    item {
                        Text(strings.navProjects, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + activeProjects.map { it.id },
                            selected = projectId,
                            labelOf = { id ->
                                id?.let { pid -> activeProjects.firstOrNull { it.id == pid }?.name } ?: strings.none
                            },
                            onSelect = { projectId = it }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(strings.description) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = receiptUrl,
                        onValueChange = { receiptUrl = it },
                        label = { Text(strings.receiptLink) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMsg != null) {
                    item {
                        Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!InputValidator.isPositiveAmount(amountStr)) {
                        errorMsg = strings.invalidInput
                        return@Button
                    }
                    val parsedDate = Formatters.parseDateOrNull(dateStr) ?: System.currentTimeMillis()
                    onSave(
                        (initial ?: TransactionEntity(
                            type = type,
                            amount = 0.0,
                            category = category
                        )).copy(
                            type = type,
                            amount = amountStr.toDouble(),
                            currency = currency,
                            category = category,
                            date = parsedDate,
                            paymentMethod = method,
                            clientId = clientId,
                            projectId = projectId,
                            description = description.trim(),
                            receiptUrl = receiptUrl.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_tx_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
