package com.example.ui.screens.marketing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Campaign
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
import com.example.data.local.CampaignEntity
import com.example.data.local.ContentItemEntity
import com.example.domain.model.CampaignStatus
import com.example.domain.model.ContentStatus
import com.example.domain.model.CurrencyCode
import com.example.domain.model.SocialChannel
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun MarketingScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Campaigns, 1 = Content Calendar
    var showCampaignForm by remember { mutableStateOf(false) }
    var editingCampaign by remember { mutableStateOf<CampaignEntity?>(null) }
    var showContentForm by remember { mutableStateOf(false) }
    var editingContent by remember { mutableStateOf<ContentItemEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        editingCampaign = null
                        showCampaignForm = true
                    } else {
                        editingContent = null
                        showContentForm = true
                    }
                },
                modifier = Modifier.testTag("add_marketing_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.add)
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(strings.campaignsTab) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(strings.contentCalendarTab) })
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                if (state.campaigns.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.Campaign,
                        title = strings.emptyMarketingTitle,
                        description = strings.emptyMarketingDesc,
                        actionLabel = strings.addCampaign,
                        onAction = { showCampaignForm = true }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.campaigns, key = { it.id }) { c ->
                            BasluneCard(onClick = { editingCampaign = c; showCampaignForm = true }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${strings.label(c.channel)} • ${strings.leadsGenerated}: ${c.leadsGenerated}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        SampleOrStarterBadge(c.isSample)
                                        StatusBadge(strings.label(c.status), BadgeTone.GOLD)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "${strings.adSpend}: ${Formatters.formatMoney(c.adSpend, c.currency, lang)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            } else {
                if (state.contentItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.Campaign,
                        title = strings.emptyMarketingTitle,
                        description = strings.emptyMarketingDesc,
                        actionLabel = strings.addContentItem,
                        onAction = { showContentForm = true }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.contentItems, key = { it.id }) { item ->
                            BasluneCard(onClick = { editingContent = item; showContentForm = true }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${strings.label(item.platform)} • ${item.contentType} • ${Formatters.formatDate(item.plannedDate)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        SampleOrStarterBadge(item.isSample)
                                        StatusBadge(strings.label(item.status), BadgeTone.NEUTRAL)
                                    }
                                }
                                if (item.caption.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(item.caption, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCampaignForm) {
        CampaignFormDialog(
            initial = editingCampaign,
            onDismiss = { showCampaignForm = false },
            onSave = { viewModel.saveCampaign(it); showCampaignForm = false },
            onDelete = editingCampaign?.let { c -> { viewModel.deleteCampaign(c.id); showCampaignForm = false } }
        )
    }

    if (showContentForm) {
        ContentFormDialog(
            initial = editingContent,
            campaigns = state.campaigns,
            onDismiss = { showContentForm = false },
            onSave = { viewModel.saveContentItem(it); showContentForm = false },
            onDelete = editingContent?.let { c -> { viewModel.deleteContentItem(c.id); showContentForm = false } }
        )
    }
}

@Composable
private fun CampaignFormDialog(
    initial: CampaignEntity?,
    onDismiss: () -> Unit,
    onSave: (CampaignEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var channel by remember { mutableStateOf(initial?.channel ?: SocialChannel.INSTAGRAM) }
    var status by remember { mutableStateOf(initial?.status ?: CampaignStatus.PLANNED) }
    var spend by remember { mutableStateOf(if ((initial?.adSpend ?: 0.0) > 0) initial!!.adSpend.toString() else "") }
    var currency by remember { mutableStateOf(initial?.currency ?: CurrencyCode.USD) }
    var leads by remember { mutableStateOf((initial?.leadsGenerated ?: 0).toString()) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addCampaign) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("${strings.campaignName} *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Text(strings.channel, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = SocialChannel.entries, selected = channel, labelOf = { strings.label(it) }, onSelect = { channel = it })
                }
                item {
                    Text(strings.status, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = CampaignStatus.entries, selected = status, labelOf = { strings.label(it) }, onSelect = { status = it })
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = spend, onValueChange = { spend = it }, label = { Text(strings.adSpend) }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = leads, onValueChange = { leads = it }, label = { Text(strings.leadsGenerated) }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    FilterChipRow(items = CurrencyCode.entries, selected = currency, labelOf = { it.name }, onSelect = { currency = it })
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(strings.notes) }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(strings.delete, color = ErrorRed) }
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    onSave(
                        (initial ?: CampaignEntity(name = "")).copy(
                            name = name.trim(),
                            channel = channel,
                            status = status,
                            adSpend = spend.toDoubleOrNull() ?: 0.0,
                            currency = currency,
                            leadsGenerated = leads.toIntOrNull() ?: 0,
                            notes = notes.trim()
                        )
                    )
                }) { Text(strings.save) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
private fun ContentFormDialog(
    initial: ContentItemEntity?,
    campaigns: List<CampaignEntity>,
    onDismiss: () -> Unit,
    onSave: (ContentItemEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var platform by remember { mutableStateOf(initial?.platform ?: SocialChannel.INSTAGRAM) }
    var contentType by remember { mutableStateOf(initial?.contentType ?: "Reel / Post") }
    var caption by remember { mutableStateOf(initial?.caption ?: "") }
    var plannedDateStr by remember { mutableStateOf(Formatters.formatDate(initial?.plannedDate ?: System.currentTimeMillis())) }
    var status by remember { mutableStateOf(initial?.status ?: ContentStatus.IDEA) }
    var mediaLink by remember { mutableStateOf(initial?.mediaLink ?: "") }
    var perfNotes by remember { mutableStateOf(initial?.performanceNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addContentItem) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("${strings.taskTitle} *") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text(strings.channel, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = SocialChannel.entries, selected = platform, labelOf = { strings.label(it) }, onSelect = { platform = it })
                }
                item {
                    Text(strings.status, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = ContentStatus.entries, selected = status, labelOf = { strings.label(it) }, onSelect = { status = it })
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = contentType, onValueChange = { contentType = it }, label = { Text(strings.contentType) }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = plannedDateStr, onValueChange = { plannedDateStr = it }, label = { Text(strings.plannedDate) }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text(strings.caption) }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = mediaLink, onValueChange = { mediaLink = it }, label = { Text(strings.mediaLink) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = perfNotes, onValueChange = { perfNotes = it }, label = { Text(strings.performanceNotes) }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(strings.delete, color = ErrorRed) }
                Button(onClick = {
                    if (title.isBlank()) return@Button
                    onSave(
                        (initial ?: ContentItemEntity(title = "")).copy(
                            title = title.trim(),
                            platform = platform,
                            contentType = contentType.trim(),
                            caption = caption.trim(),
                            plannedDate = Formatters.parseDateOrNull(plannedDateStr) ?: System.currentTimeMillis(),
                            status = status,
                            mediaLink = mediaLink.trim(),
                            performanceNotes = perfNotes.trim()
                        )
                    )
                }) { Text(strings.save) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
