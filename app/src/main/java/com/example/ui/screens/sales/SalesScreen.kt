package com.example.ui.screens.sales

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.TrendingUp
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
import com.example.data.local.OpportunityEntity
import com.example.domain.logic.AnalyticsEngine
import com.example.domain.model.CurrencyCode
import com.example.domain.model.OpportunityStage
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun SalesScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    openAddImmediately: Boolean = false,
    onConsumeQuickAction: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var showKanban by remember { mutableStateOf(true) }
    var showForm by remember { mutableStateOf(false) }
    var editingOpp by remember { mutableStateOf<OpportunityEntity?>(null) }

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            editingOpp = null
            showForm = true
            onConsumeQuickAction()
        }
    }

    val pipelineVal = remember(state.opportunities) { AnalyticsEngine.openPipelineValue(state.opportunities) }
    val conversionRate = remember(state.opportunities) { AnalyticsEngine.pipelineConversionRate(state.opportunities) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingOpp = null
                    showForm = true
                },
                modifier = Modifier.testTag("add_opportunity_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.addOpportunity)
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
            BasluneCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.pipelineValue, style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${Formatters.formatMoney(pipelineVal.iqd, CurrencyCode.IQD, lang)}  •  ${Formatters.formatMoney(pipelineVal.usd, CurrencyCode.USD, lang)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(strings.conversionRate, style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${conversionRate.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilterChipRow(
                items = listOf(true, false),
                selected = showKanban,
                labelOf = { if (it) strings.viewKanban else strings.viewList },
                onSelect = { showKanban = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (state.opportunities.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.TrendingUp,
                    title = strings.emptySalesTitle,
                    description = strings.emptySalesDesc,
                    actionLabel = strings.addOpportunity,
                    onAction = {
                        editingOpp = null
                        showForm = true
                    }
                )
            } else if (showKanban) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OpportunityStage.entries.forEach { stage ->
                        val items = state.opportunities.filter { it.stage == stage }
                        Column(modifier = Modifier.width(260.dp).fillMaxHeight()) {
                            StatusBadge("${strings.label(stage)} (${items.size})", BadgeTone.DARK)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 88.dp)
                            ) {
                                items(items, key = { it.id }) { opp ->
                                    OpportunityCard(
                                        opp = opp,
                                        onEdit = {
                                            editingOpp = opp
                                            showForm = true
                                        },
                                        onConvert = {
                                            viewModel.convertOpportunity(opp, strings.convertedSuccess)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.opportunities, key = { it.id }) { opp ->
                        OpportunityCard(
                            opp = opp,
                            onEdit = {
                                editingOpp = opp
                                showForm = true
                            },
                            onConvert = {
                                viewModel.convertOpportunity(opp, strings.convertedSuccess)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showForm) {
        OpportunityFormDialog(
            initial = editingOpp,
            state = state,
            onDismiss = { showForm = false },
            onSave = {
                viewModel.saveOpportunity(it)
                showForm = false
            },
            onDelete = editingOpp?.let { o ->
                {
                    viewModel.deleteOpportunity(o.id)
                    showForm = false
                }
            }
        )
    }
}

@Composable
private fun OpportunityCard(
    opp: OpportunityEntity,
    onEdit: () -> Unit,
    onConvert: () -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    BasluneCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(opp.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    opp.potentialClientName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SampleOrStarterBadge(opp.isSample)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                Formatters.formatMoney(opp.estimatedValue, opp.currency, lang),
                style = MaterialTheme.typography.labelLarge
            )
            Text("${opp.probability}%", style = MaterialTheme.typography.labelMedium)
        }
        if (opp.requestedServiceName.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(opp.requestedServiceName, style = MaterialTheme.typography.labelSmall)
        }
        if (opp.convertedProjectId == null && opp.stage != OpportunityStage.LOST) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.convertToClientProject, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun OpportunityFormDialog(
    initial: OpportunityEntity?,
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (OpportunityEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var potentialClient by remember { mutableStateOf(initial?.potentialClientName ?: "") }
    var selectedServiceId by remember { mutableStateOf(initial?.requestedServiceId) }
    var serviceName by remember { mutableStateOf(initial?.requestedServiceName ?: "") }
    var valueText by remember { mutableStateOf(if ((initial?.estimatedValue ?: 0.0) > 0) initial!!.estimatedValue.toString() else "") }
    var currency by remember { mutableStateOf(initial?.currency ?: state.settings.defaultCurrency) }
    var stage by remember { mutableStateOf(initial?.stage ?: OpportunityStage.NEW_LEAD) }
    var probabilityText by remember { mutableStateOf((initial?.probability ?: 30).toString()) }
    var source by remember { mutableStateOf(initial?.source ?: "") }
    var followUpStr by remember { mutableStateOf(initial?.nextFollowUpDate?.let { Formatters.formatDate(it) } ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addOpportunity else strings.editOpportunity) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("${strings.opportunityTitle} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = potentialClient,
                        onValueChange = { potentialClient = it },
                        label = { Text("${strings.potentialClient} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (state.services.isNotEmpty()) {
                    item {
                        Text(strings.requestedService, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + state.services.map { it.id },
                            selected = selectedServiceId,
                            labelOf = { id ->
                                id?.let { sid -> state.services.firstOrNull { it.id == sid }?.name } ?: strings.none
                            },
                            onSelect = { id ->
                                selectedServiceId = id
                                id?.let { sid ->
                                    state.services.firstOrNull { it.id == sid }?.let { svc ->
                                        serviceName = svc.name
                                        if (valueText.isBlank()) valueText = svc.startingPrice.toString()
                                    }
                                }
                            }
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { valueText = it },
                            label = { Text(strings.estimatedValue) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
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
                    Text(strings.currentStage, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = OpportunityStage.entries,
                        selected = stage,
                        labelOf = { strings.label(it) },
                        onSelect = { stage = it }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = probabilityText,
                            onValueChange = { probabilityText = it },
                            label = { Text("${strings.probability} %") },
                            singleLine = true,
                            modifier = Modifier.width(110.dp)
                        )
                        OutlinedTextField(
                            value = followUpStr,
                            onValueChange = { followUpStr = it },
                            label = { Text("${strings.followUpDate} (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(strings.delete, color = ErrorRed) }
                }
                Button(
                    onClick = {
                        if (title.isBlank() || potentialClient.isBlank()) return@Button
                        onSave(
                            (initial ?: OpportunityEntity(title = "", potentialClientName = "")).copy(
                                title = title.trim(),
                                potentialClientName = potentialClient.trim(),
                                requestedServiceId = selectedServiceId,
                                requestedServiceName = serviceName.trim(),
                                estimatedValue = valueText.toDoubleOrNull() ?: 0.0,
                                currency = currency,
                                stage = stage,
                                probability = (probabilityText.toIntOrNull() ?: 25).coerceIn(0, 100),
                                source = source.trim(),
                                nextFollowUpDate = Formatters.parseDateOrNull(followUpStr),
                                notes = notes.trim()
                            )
                        )
                    }
                ) {
                    Text(strings.save)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
