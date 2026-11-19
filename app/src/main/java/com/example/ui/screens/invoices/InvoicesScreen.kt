package com.example.ui.screens.invoices

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.core.print.InvoicePdfPrinter
import com.example.core.security.InputValidator
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.CurrencyCode
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.PaymentMethod
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun InvoicesScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    openCreateImmediately: Boolean = false,
    onConsumeQuickAction: () -> Unit = {},
    onOpenInvoice: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var statusFilter by remember { mutableStateOf<InvoiceStatus?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    LaunchedEffect(openCreateImmediately) {
        if (openCreateImmediately) {
            showCreate = true
            onConsumeQuickAction()
        }
    }

    val filtered = remember(state.invoices, statusFilter) {
        if (statusFilter == null) state.invoices else state.invoices.filter { it.status == statusFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                modifier = Modifier.testTag("create_invoice_fab")
            ) {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = strings.createInvoice)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            FilterChipRow(
                items = listOf(null) + InvoiceStatus.entries,
                selected = statusFilter,
                labelOf = { it?.let { s -> strings.label(s) } ?: strings.all },
                onSelect = { statusFilter = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.ReceiptLong,
                    title = strings.emptyInvoicesTitle,
                    description = strings.emptyInvoicesDesc,
                    actionLabel = strings.createInvoice,
                    onAction = { showCreate = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtered, key = { it.id }) { inv ->
                        val remaining = (inv.total - inv.paidAmount).coerceAtLeast(0.0)
                        BasluneCard(
                            onClick = { onOpenInvoice(inv.id) },
                            modifier = Modifier.testTag("invoice_card_${inv.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inv.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                                    Text(inv.clientNameSnapshot, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SampleOrStarterBadge(inv.isSample)
                                    StatusBadge(
                                        text = strings.label(inv.status),
                                        tone = when (inv.status) {
                                            InvoiceStatus.PAID -> BadgeTone.SUCCESS
                                            InvoiceStatus.PARTIALLY_PAID -> BadgeTone.WARNING
                                            InvoiceStatus.OVERDUE -> BadgeTone.ERROR
                                            else -> BadgeTone.NEUTRAL
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${strings.total}: ${Formatters.formatMoney(inv.total, inv.currency, lang)}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    "${strings.remainingBalance}: ${Formatters.formatMoney(remaining, inv.currency, lang)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (remaining > 0.01) ErrorRed else SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        InvoiceBuilderDialog(
            state = state,
            onDismiss = { showCreate = false },
            onSave = { inv, items ->
                viewModel.saveInvoice(inv, items)
                showCreate = false
            }
        )
    }
}

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val invoice = state.invoices.firstOrNull { it.id == invoiceId } ?: return
    val items = remember(state.invoiceItems, invoiceId) { state.invoiceItems.filter { it.invoiceId == invoiceId } }
    val payments = remember(state.payments, invoiceId) { state.payments.filter { it.invoiceId == invoiceId } }
    val remaining = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)

    var showPaymentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("invoice_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BasluneCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(invoice.invoiceNumber, style = MaterialTheme.typography.headlineSmall)
                        Text(invoice.clientNameSnapshot, style = MaterialTheme.typography.titleMedium)
                    }
                    StatusBadge(strings.label(invoice.status), BadgeTone.GOLD)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("${strings.issueDate}: ${Formatters.formatDate(invoice.issueDate)}", style = MaterialTheme.typography.bodySmall)
                Text("${strings.dueDate}: ${Formatters.formatDate(invoice.dueDate)}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (invoice.status == InvoiceStatus.DRAFT) {
                        OutlinedButton(onClick = { viewModel.markInvoiceSent(invoice) }) {
                            Text(strings.markAsSent)
                        }
                    }
                    if (remaining > 0.01 && invoice.status != InvoiceStatus.CANCELLED) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.testTag("record_payment_button")
                        ) {
                            Text(strings.recordPayment)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            InvoicePdfPrinter.printInvoice(context, invoice, items, state.settings, lang)
                        }
                    ) {
                        Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.printOrExportPdf)
                    }
                }
            }
        }

        item {
            BasluneCard {
                Text(strings.lineItems, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.title} × ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                        Text(Formatters.formatMoney(item.lineTotal, invoice.currency, lang), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.subtotal, style = MaterialTheme.typography.bodySmall)
                    Text(Formatters.formatMoney(invoice.subtotal, invoice.currency, lang))
                }
                if (invoice.discount > 0.0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.discount, style = MaterialTheme.typography.bodySmall)
                        Text("-${Formatters.formatMoney(invoice.discount, invoice.currency, lang)}")
                    }
                }
                if (invoice.tax > 0.0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.tax, style = MaterialTheme.typography.bodySmall)
                        Text(Formatters.formatMoney(invoice.tax, invoice.currency, lang))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.total, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(Formatters.formatMoney(invoice.total, invoice.currency, lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.paidAmount, style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                    Text(Formatters.formatMoney(invoice.paidAmount, invoice.currency, lang), color = SuccessGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.remainingBalance, style = MaterialTheme.typography.titleSmall, color = ErrorRed)
                    Text(Formatters.formatMoney(remaining, invoice.currency, lang), color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            BasluneCard {
                Text("${strings.paymentsHistory} (${payments.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (payments.isEmpty()) {
                    Text("—", style = MaterialTheme.typography.bodySmall)
                } else {
                    payments.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${Formatters.formatDate(p.paymentDate)} • ${strings.label(p.method)}", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatMoney(p.amount, p.currency, lang), style = MaterialTheme.typography.labelLarge, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordPaymentDialog(
            maxRemaining = remaining,
            currency = invoice.currency,
            onDismiss = { showPaymentDialog = false },
            onSave = { amt, method, notes ->
                viewModel.recordInvoicePayment(invoice, amt, method, notes)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun RecordPaymentDialog(
    maxRemaining: Double,
    currency: CurrencyCode,
    onDismiss: () -> Unit,
    onSave: (Double, PaymentMethod, String) -> Unit
) {
    val strings = LocalStrings.current
    var amountStr by remember { mutableStateOf(maxRemaining.toString()) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${strings.recordPayment} (${currency.name})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(strings.amount) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )
                Text(strings.paymentMethod, style = MaterialTheme.typography.labelMedium)
                FilterChipRow(
                    items = PaymentMethod.entries,
                    selected = method,
                    labelOf = { strings.label(it) },
                    onSelect = { method = it }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notes) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(error!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!InputValidator.isPositiveAmount(amountStr)) {
                        error = strings.invalidInput
                        return@Button
                    }
                    onSave(amountStr.toDouble(), method, notes.trim())
                },
                modifier = Modifier.testTag("save_payment_button")
            ) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
private fun InvoiceBuilderDialog(
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (InvoiceEntity, List<InvoiceItemEntity>) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val activeClients = state.clients.filter { !it.isArchived }
    val activeProjects = state.projects.filter { !it.isArchived }

    var selectedClientId by remember { mutableStateOf(activeClients.firstOrNull()?.id) }
    var customClientName by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var currency by remember { mutableStateOf(state.settings.defaultCurrency) }
    var itemTitle by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("1") }
    var unitPriceStr by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("0") }
    var taxStr by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<InvoiceItemEntity>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val (_, _, previewTotal) = remember(items, discountStr, taxStr) {
        FinancialCalculator.computeInvoiceTotals(
            items.map { it.lineTotal },
            discountStr.toDoubleOrNull() ?: 0.0,
            taxStr.toDoubleOrNull() ?: 0.0
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.createInvoice) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeClients.isNotEmpty()) {
                    item {
                        Text(strings.client, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = activeClients.map { it.id },
                            selected = selectedClientId ?: activeClients.first().id,
                            labelOf = { id -> activeClients.firstOrNull { it.id == id }?.fullName ?: "" },
                            onSelect = { selectedClientId = it }
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = customClientName,
                            onValueChange = { customClientName = it },
                            label = { Text("${strings.clientName} *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (activeProjects.isNotEmpty()) {
                    item {
                        Text(strings.navProjects, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + activeProjects.map { it.id },
                            selected = selectedProjectId,
                            labelOf = { id -> id?.let { pid -> activeProjects.firstOrNull { it.id == pid }?.name } ?: strings.none },
                            onSelect = { selectedProjectId = it }
                        )
                    }
                }
                item {
                    FilterChipRow(
                        items = CurrencyCode.entries,
                        selected = currency,
                        labelOf = { it.name },
                        onSelect = { currency = it }
                    )
                }
                if (state.services.isNotEmpty()) {
                    item {
                        Text(strings.navServices, style = MaterialTheme.typography.labelSmall)
                        FilterChipRow(
                            items = state.services,
                            selected = state.services.first(),
                            labelOf = { it.name },
                            onSelect = { svc ->
                                itemTitle = svc.name
                                unitPriceStr = svc.startingPrice.toString()
                            }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text(strings.serviceOrItem) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text(strings.quantity) },
                            singleLine = true,
                            modifier = Modifier.width(72.dp)
                        )
                        OutlinedTextField(
                            value = unitPriceStr,
                            onValueChange = { unitPriceStr = it },
                            label = { Text(strings.unitPrice) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val q = (qtyStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                                val p = unitPriceStr.toDoubleOrNull() ?: 0.0
                                if (itemTitle.isNotBlank() && p >= 0.0) {
                                    items = items + InvoiceItemEntity(
                                        invoiceId = 0L,
                                        title = itemTitle.trim(),
                                        quantity = q,
                                        unitPrice = p,
                                        lineTotal = q * p
                                    )
                                    itemTitle = ""
                                    unitPriceStr = ""
                                    qtyStr = "1"
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.AddCircle, contentDescription = strings.addLineItem)
                        }
                    }
                }
                items.forEachIndexed { idx, it ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${it.title} ×${it.quantity}", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatMoney(it.lineTotal, currency, lang), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text(strings.discount) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = taxStr,
                            onValueChange = { taxStr = it },
                            label = { Text(strings.tax) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(
                        "${strings.total}: ${Formatters.formatMoney(previewTotal, currency, lang)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMsg != null) {
                    item { Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clientSnapshot = selectedClientId?.let { cid ->
                        activeClients.firstOrNull { it.id == cid }?.fullName
                    } ?: customClientName.trim()

                    if (clientSnapshot.isBlank() || items.isEmpty()) {
                        errorMsg = strings.invalidInput
                        return@Button
                    }
                    onSave(
                        InvoiceEntity(
                            invoiceNumber = "",
                            clientId = selectedClientId,
                            clientNameSnapshot = clientSnapshot,
                            projectId = selectedProjectId,
                            currency = currency,
                            discount = discountStr.toDoubleOrNull() ?: 0.0,
                            tax = taxStr.toDoubleOrNull() ?: 0.0,
                            status = InvoiceStatus.SENT,
                            notes = notes.trim()
                        ),
                        items
                    )
                },
                modifier = Modifier.testTag("save_invoice_button")
            ) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
