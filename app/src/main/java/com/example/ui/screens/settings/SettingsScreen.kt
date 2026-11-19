package com.example.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.BasluneCard
import com.example.core.design.FilterChipRow
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalStrings
import com.example.domain.model.CurrencyCode
import com.example.domain.model.UserRole
import com.example.ui.navigation.AppRoutes
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun SettingsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onNavigate: (String) -> Unit
) {
    val strings = LocalStrings.current
    val s = state.settings

    var businessName by remember(s.businessName) { mutableStateOf(s.businessName) }
    var tagline by remember(s.tagline) { mutableStateOf(s.tagline) }
    var location by remember(s.location) { mutableStateOf(s.location) }
    var email by remember(s.contactEmail) { mutableStateOf(s.contactEmail) }
    var instagram by remember(s.instagram) { mutableStateOf(s.instagram) }
    var tiktok by remember(s.tiktok) { mutableStateOf(s.tiktok) }
    var streetAddress by remember(s.streetAddress) { mutableStateOf(s.streetAddress) }
    var phone by remember(s.phone) { mutableStateOf(s.phone) }
    var taxId by remember(s.taxId) { mutableStateOf(s.taxId) }
    var bankDetails by remember(s.bankDetails) { mutableStateOf(s.bankDetails) }
    var rateText by remember(s.usdToIqdRate) { mutableStateOf(s.usdToIqdRate.toInt().toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Language & Direction
        item {
            BasluneCard {
                Text(strings.languageSettings, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipRow(
                    items = AppLanguage.entries,
                    selected = s.language,
                    labelOf = { it.nativeName },
                    onSelect = { viewModel.setLanguage(it) },
                    modifier = Modifier.testTag("language_selector")
                )
            }
        }

        // Appearance
        item {
            BasluneCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.darkMode, style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = s.darkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Currency & Manual Exchange Rate
        item {
            BasluneCard {
                Text(strings.currencyAndRate, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipRow(
                    items = CurrencyCode.entries,
                    selected = s.defaultCurrency,
                    labelOf = { it.name },
                    onSelect = { viewModel.setDefaultCurrency(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text(strings.usdToIqdRate) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        rateText.toDoubleOrNull()?.takeIf { it > 0 }?.let {
                            viewModel.setExchangeRate(it)
                            viewModel.showMessage(strings.copiedOrSaved)
                        }
                    }) {
                        Text(strings.updateRate)
                    }
                }
            }
        }

        // Backup Center shortcut
        item {
            BasluneCard(onClick = { onNavigate(AppRoutes.BACKUP) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Outlined.Backup, contentDescription = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.backupCenterTitle, style = MaterialTheme.typography.titleMedium)
                        Text(strings.exportAllBackup, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Sample Data Controls
        item {
            BasluneCard {
                Text(strings.sampleDataControl, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (s.sampleDataActive) {
                    OutlinedButton(onClick = { viewModel.deactivateSampleData() }) {
                        Text(strings.deactivateSampleData)
                    }
                } else {
                    Button(onClick = { viewModel.activateSampleData() }) {
                        Text(strings.activateSampleData)
                    }
                }
            }
        }

        // Business Profile (BASLUNE Studio)
        item {
            BasluneCard {
                Text(strings.businessProfile, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text(strings.studioName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Tagline") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(strings.city) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(strings.email) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = instagram, onValueChange = { instagram = it }, label = { Text("Instagram") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = tiktok, onValueChange = { tiktok = it }, label = { Text("TikTok") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(strings.phone) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = streetAddress, onValueChange = { streetAddress = it }, label = { Text(strings.address) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = taxId, onValueChange = { taxId = it }, label = { Text("Tax / Registration ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = bankDetails, onValueChange = { bankDetails = it }, label = { Text("Bank / Payment Details") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.updateBusinessProfile(
                            businessName, tagline, location, email, instagram, tiktok, streetAddress, phone, taxId, bankDetails
                        )
                        viewModel.showMessage(strings.copiedOrSaved)
                    }
                ) {
                    Text(strings.save)
                }
            }
        }

        // Local-first Security Notice
        item {
            BasluneCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                    Text(strings.localFirstSecurityTitle, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.localFirstSecurityDesc, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${strings.activeRole}: ${strings.label(s.activeRole)} (${UserRole.OWNER.name} has full backup & settings access)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
