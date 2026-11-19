package com.example.ui.screens.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DesignServices
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
import com.example.data.local.ServiceEntity
import com.example.domain.model.CurrencyCode
import com.example.domain.model.ServiceCategory
import com.example.domain.model.ServicePriceType
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun ServicesScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }

    val filtered = remember(state.services, selectedCategory) {
        if (selectedCategory == null) state.services else state.services.filter { it.category == selectedCategory }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingService = null
                    showForm = true
                },
                modifier = Modifier.testTag("add_service_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.addService)
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
                items = listOf(null) + ServiceCategory.entries,
                selected = selectedCategory,
                labelOf = { it?.let { c -> strings.label(c) } ?: strings.all },
                onSelect = { selectedCategory = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.DesignServices,
                    title = strings.emptyServicesTitle,
                    description = strings.emptyServicesDesc,
                    actionLabel = strings.addService,
                    onAction = {
                        editingService = null
                        showForm = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtered, key = { it.id }) { svc ->
                        BasluneCard(
                            onClick = {
                                editingService = svc
                                showForm = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(svc.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${strings.label(svc.category)} • ${strings.label(svc.priceType)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SampleOrStarterBadge(svc.isSample)
                                    StatusBadge(
                                        text = if (svc.isActive) strings.activeService else strings.inactiveService,
                                        tone = if (svc.isActive) BadgeTone.SUCCESS else BadgeTone.NEUTRAL
                                    )
                                }
                            }
                            if (svc.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(svc.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${strings.startingPrice}: ${Formatters.formatMoney(svc.startingPrice, svc.currency, lang)}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                if (svc.estimatedDelivery.isNotBlank()) {
                                    Text(
                                        svc.estimatedDelivery,
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
    }

    if (showForm) {
        ServiceFormDialog(
            initial = editingService,
            defaultCurrency = state.settings.defaultCurrency,
            onDismiss = { showForm = false },
            onSave = {
                viewModel.saveService(it)
                showForm = false
            },
            onDelete = editingService?.let { s ->
                {
                    viewModel.deleteService(s.id)
                    showForm = false
                }
            }
        )
    }
}

@Composable
private fun ServiceFormDialog(
    initial: ServiceEntity?,
    defaultCurrency: CurrencyCode,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: ServiceCategory.WEBSITE_DESIGN_DEVELOPMENT) }
    var priceType by remember { mutableStateOf(initial?.priceType ?: ServicePriceType.FIXED) }
    var price by remember { mutableStateOf(if ((initial?.startingPrice ?: 0.0) > 0) initial!!.startingPrice.toString() else "") }
    var currency by remember { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var delivery by remember { mutableStateOf(initial?.estimatedDelivery ?: "") }
    var features by remember { mutableStateOf(initial?.includedFeatures ?: "") }
    var addOns by remember { mutableStateOf(initial?.optionalAddOns ?: "") }
    var reqInfo by remember { mutableStateOf(initial?.requiredInfo ?: "") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addService else strings.editService) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("${strings.serviceName} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(strings.category, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = ServiceCategory.entries,
                        selected = category,
                        labelOf = { strings.label(it) },
                        onSelect = { category = it }
                    )
                }
                item {
                    Text(strings.priceType, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = ServicePriceType.entries,
                        selected = priceType,
                        labelOf = { strings.label(it) },
                        onSelect = { priceType = it }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text(strings.startingPrice) },
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
                    OutlinedTextField(
                        value = delivery,
                        onValueChange = { delivery = it },
                        label = { Text(strings.deliveryTime) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        label = { Text(strings.includedFeatures) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = addOns,
                        onValueChange = { addOns = it },
                        label = { Text(strings.optionalAddOns) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = reqInfo,
                        onValueChange = { reqInfo = it },
                        label = { Text(strings.requiredInfo) },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isActive) strings.activeService else strings.inactiveService)
                    }
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
                        if (name.isBlank()) return@Button
                        onSave(
                            (initial ?: ServiceEntity(name = "")).copy(
                                name = name.trim(),
                                description = description.trim(),
                                category = category,
                                priceType = priceType,
                                startingPrice = price.toDoubleOrNull() ?: 0.0,
                                currency = currency,
                                estimatedDelivery = delivery.trim(),
                                includedFeatures = features.trim(),
                                optionalAddOns = addOns.trim(),
                                requiredInfo = reqInfo.trim(),
                                isActive = isActive
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
