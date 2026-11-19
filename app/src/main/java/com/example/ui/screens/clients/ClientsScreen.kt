package com.example.ui.screens.clients

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.core.security.InputValidator
import com.example.data.local.ClientCommunicationEntity
import com.example.data.local.ClientEntity
import com.example.domain.model.ClientStatus
import com.example.domain.model.ClientType
import com.example.ui.navigation.AppRoutes
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun ClientsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    openAddImmediately: Boolean = false,
    onConsumeQuickAction: () -> Unit = {},
    onOpenClient: (Long) -> Unit
) {
    val strings = LocalStrings.current
    var query by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ClientStatus?>(null) }
    var sortNewest by remember { mutableStateOf(true) }
    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            editingClient = null
            showForm = true
            onConsumeQuickAction()
        }
    }

    val filtered = remember(state.clients, query, selectedStatus, sortNewest) {
        state.clients
            .filter { client ->
                val matchesStatus = if (selectedStatus == null) !client.isArchived else client.status == selectedStatus
                val q = query.trim().lowercase()
                val matchesQuery = q.isEmpty() ||
                    client.fullName.lowercase().contains(q) ||
                    client.companyName.lowercase().contains(q) ||
                    client.city.lowercase().contains(q) ||
                    client.tags.lowercase().contains(q)
                matchesStatus && matchesQuery
            }
            .let { list ->
                if (sortNewest) list.sortedByDescending { it.dateAdded }
                else list.sortedBy { it.fullName.lowercase() }
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingClient = null
                    showForm = true
                },
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = strings.addClient)
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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.search) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_search_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipRow(
                        items = listOf(null) + ClientStatus.entries,
                        selected = selectedStatus,
                        labelOf = { it?.let { s -> strings.label(s) } ?: strings.all },
                        onSelect = { selectedStatus = it }
                    )
                }
                IconButton(onClick = { sortNewest = !sortNewest }) {
                    Icon(Icons.Outlined.SortByAlpha, contentDescription = strings.sort)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.People,
                    title = strings.emptyClientsTitle,
                    description = strings.emptyClientsDesc,
                    actionLabel = strings.addClient,
                    onAction = {
                        editingClient = null
                        showForm = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtered, key = { it.id }) { client ->
                        BasluneCard(
                            onClick = { onOpenClient(client.id) },
                            modifier = Modifier.testTag("client_card_${client.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(client.fullName, style = MaterialTheme.typography.titleMedium)
                                    if (client.companyName.isNotBlank()) {
                                        Text(
                                            client.companyName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SampleOrStarterBadge(client.isSample)
                                    StatusBadge(
                                        text = strings.label(client.status),
                                        tone = when (client.status) {
                                            ClientStatus.ACTIVE -> BadgeTone.SUCCESS
                                            ClientStatus.NEGOTIATING -> BadgeTone.WARNING
                                            ClientStatus.ARCHIVED -> BadgeTone.NEUTRAL
                                            else -> BadgeTone.GOLD
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
                                    text = "${client.city}, ${client.country}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = client.phone.ifBlank { client.email },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        ClientFormDialog(
            initial = editingClient,
            onDismiss = { showForm = false },
            onSave = {
                viewModel.saveClient(it)
                showForm = false
            }
        )
    }
}

@Composable
fun ClientDetailScreen(
    clientId: Long,
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val client = state.clients.firstOrNull { it.id == clientId } ?: return

    val relatedProjects = remember(state.projects, clientId) {
        state.projects.filter { it.clientId == clientId }
    }
    val relatedInvoices = remember(state.invoices, clientId) {
        state.invoices.filter { it.clientId == clientId }
    }
    val relatedTransactions = remember(state.transactions, clientId) {
        state.transactions.filter { it.clientId == clientId }
    }
    val comms = remember(state.communications, clientId) {
        state.communications.filter { it.clientId == clientId }
    }

    var showEdit by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showCommDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("client_detail_screen"),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(client.fullName, style = MaterialTheme.typography.headlineSmall)
                        if (client.companyName.isNotBlank()) {
                            Text(client.companyName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "${client.clientCode} • ${strings.label(client.clientType)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(strings.label(client.status), BadgeTone.GOLD)
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (client.email.isNotBlank()) Text("${strings.email}: ${client.email}", style = MaterialTheme.typography.bodySmall)
                if (client.phone.isNotBlank()) Text("${strings.phone}: ${client.phone}", style = MaterialTheme.typography.bodySmall)
                if (client.whatsapp.isNotBlank()) Text("${strings.whatsapp}: ${client.whatsapp}", style = MaterialTheme.typography.bodySmall)
                Text("${strings.city}: ${client.city}, ${client.country}", style = MaterialTheme.typography.bodySmall)
                if (client.industry.isNotBlank()) Text("${strings.industry}: ${client.industry}", style = MaterialTheme.typography.bodySmall)
                if (client.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(client.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showEdit = true }) {
                        Text(strings.edit)
                    }
                    if (!client.isArchived) {
                        OutlinedButton(onClick = { showArchiveConfirm = true }) {
                            Text(strings.archive)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "${strings.relatedProjects} (${relatedProjects.size})")
        }
        item {
            if (relatedProjects.isEmpty()) {
                BasluneCard { Text(strings.emptyProjectsTitle, style = MaterialTheme.typography.bodySmall) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    relatedProjects.forEach { p ->
                        BasluneCard(onClick = { onNavigate(AppRoutes.projectDetail(p.id)) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(p.name, style = MaterialTheme.typography.titleSmall)
                                StatusBadge(strings.label(p.status), BadgeTone.GOLD)
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "${strings.financialHistory} (${relatedInvoices.size} / ${relatedTransactions.size})")
        }
        item {
            BasluneCard {
                if (relatedInvoices.isEmpty() && relatedTransactions.isEmpty()) {
                    Text(strings.emptyFinanceTitle, style = MaterialTheme.typography.bodySmall)
                } else {
                    relatedInvoices.forEach { inv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${inv.invoiceNumber} (${strings.label(inv.status)})", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatMoney(inv.total, inv.currency, lang), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    relatedTransactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${Formatters.formatDate(tx.date)} • ${tx.description}", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatMoney(tx.amount, tx.currency, lang), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = strings.communicationHistory,
                actionLabel = strings.add,
                onActionClick = { showCommDialog = true }
            )
        }
        item {
            if (comms.isEmpty()) {
                BasluneCard { Text("—", style = MaterialTheme.typography.bodySmall) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    comms.forEach { c ->
                        BasluneCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(c.interactionType, style = MaterialTheme.typography.titleSmall)
                                Text(Formatters.formatDate(c.date), style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.summary, style = MaterialTheme.typography.bodySmall)
                            if (c.requirements.isNotBlank()) {
                                Text("${strings.requirements}: ${c.requirements}", style = MaterialTheme.typography.labelSmall)
                            }
                            c.followUpDate?.let {
                                Text("${strings.followUpDate}: ${Formatters.formatDate(it)}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEdit) {
        ClientFormDialog(
            initial = client,
            onDismiss = { showEdit = false },
            onSave = {
                viewModel.saveClient(it)
                showEdit = false
            }
        )
    }

    if (showArchiveConfirm) {
        ConfirmDestructiveDialog(
            title = strings.archive,
            message = strings.archiveClientConfirm,
            confirmLabel = strings.archive,
            onConfirm = {
                viewModel.archiveClient(client)
                showArchiveConfirm = false
                onBack()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showCommDialog) {
        CommunicationFormDialog(
            clientId = clientId,
            onDismiss = { showCommDialog = false },
            onSave = {
                viewModel.addCommunication(it)
                showCommDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(
    initial: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    val strings = LocalStrings.current
    var fullName by remember { mutableStateOf(initial?.fullName ?: "") }
    var companyName by remember { mutableStateOf(initial?.companyName ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(initial?.whatsapp ?: "") }
    var city by remember { mutableStateOf(initial?.city ?: "Erbil") }
    var country by remember { mutableStateOf(initial?.country ?: "Iraq") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var industry by remember { mutableStateOf(initial?.industry ?: "") }
    var source by remember { mutableStateOf(initial?.acquisitionSource ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: ClientStatus.ACTIVE) }
    var clientType by remember { mutableStateOf(initial?.clientType ?: ClientType.BUSINESS) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addClient else strings.editClient) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("${strings.clientName} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("client_name_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text(strings.companyName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(strings.phone) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text(strings.whatsapp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(strings.email) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(strings.city) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(strings.country) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(strings.status, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = ClientStatus.entries,
                        selected = status,
                        labelOf = { strings.label(it) },
                        onSelect = { status = it }
                    )
                }
                item {
                    Text(strings.clientType, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = ClientType.entries,
                        selected = clientType,
                        labelOf = { strings.label(it) },
                        onSelect = { clientType = it }
                    )
                }
                item {
                    OutlinedTextField(
                        value = industry,
                        onValueChange = { industry = it },
                        label = { Text(strings.industry) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text(strings.acquisitionSource) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text(strings.tags) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                    item {
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || !InputValidator.isValidEmail(email)) {
                        errorMsg = strings.invalidInput
                        return@Button
                    }
                    onSave(
                        (initial ?: ClientEntity(fullName = "")).copy(
                            fullName = fullName.trim(),
                            companyName = companyName.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            whatsapp = whatsapp.trim(),
                            city = city.trim(),
                            country = country.trim(),
                            address = address.trim(),
                            industry = industry.trim(),
                            acquisitionSource = source.trim(),
                            tags = tags.trim(),
                            notes = notes.trim(),
                            status = status,
                            clientType = clientType
                        )
                    )
                },
                modifier = Modifier.testTag("save_client_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
private fun CommunicationFormDialog(
    clientId: Long,
    onDismiss: () -> Unit,
    onSave: (ClientCommunicationEntity) -> Unit
) {
    val strings = LocalStrings.current
    var type by remember { mutableStateOf("Meeting") }
    var summary by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var followUpStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.recordCommunication) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text(strings.communicationType) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(strings.notes) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text(strings.requirements) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = followUpStr,
                    onValueChange = { followUpStr = it },
                    label = { Text("${strings.followUpDate} (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (summary.isBlank()) return@Button
                    onSave(
                        ClientCommunicationEntity(
                            clientId = clientId,
                            interactionType = type.trim(),
                            summary = summary.trim(),
                            requirements = requirements.trim(),
                            followUpDate = Formatters.parseDateOrNull(followUpStr)
                        )
                    )
                }
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
