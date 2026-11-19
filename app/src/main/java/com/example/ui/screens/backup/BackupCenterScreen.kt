package com.example.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.BasluneCard
import com.example.core.design.ConfirmDestructiveDialog
import com.example.core.localization.Formatters
import com.example.core.localization.LocalStrings
import com.example.domain.model.AppModule
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState
import kotlinx.coroutines.launch

@Composable
fun BackupCenterScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportableModules = listOf(
        AppModule.CLIENTS to strings.navClients,
        AppModule.PROJECTS to strings.navProjects,
        AppModule.TASKS to strings.navTasks,
        AppModule.SERVICES to strings.navServices,
        AppModule.SALES to strings.navSales,
        AppModule.FINANCE to strings.navFinance,
        AppModule.INVOICES to strings.navInvoices,
        AppModule.MARKETING to strings.navMarketing,
        AppModule.DOCUMENTS to strings.navDocuments,
        AppModule.KNOWLEDGE to strings.navKnowledge,
        AppModule.TEAM to strings.navTeam
    )

    var selectedModules by remember { mutableStateOf(exportableModules.map { it.first }.toSet()) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            runCatching {
                val stream = context.contentResolver.openOutputStream(uri)
                    ?: error("Unable to open selected document for writing")
                stream.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                viewModel.showMessage(strings.backupSuccess)
            }.onFailure {
                viewModel.showMessage(it.message ?: "Export error")
            }
        }
        pendingExportJson = null
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    pendingRestoreJson = content
                } else {
                    viewModel.showMessage(strings.backupInvalidError)
                }
            }.onFailure {
                viewModel.showMessage(strings.backupInvalidError)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("backup_center_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BasluneCard {
                Text(strings.backupCenterTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${strings.lastBackupDate}: ${Formatters.formatDateTime(state.settings.lastBackupTimestamp)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        scope.launch {
                            pendingExportJson = viewModel.generateBackupJson(null)
                            createBackupLauncher.launch("BASLUNE_OS_Full_Backup.json")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("export_all_backup_btn")
                ) {
                    Icon(Icons.Outlined.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.exportAllBackup)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { openBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.fillMaxWidth().testTag("import_backup_btn")
                ) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.importAndRestore)
                }
            }
        }

        item {
            BasluneCard {
                Text(strings.exportSelectedBackup, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                exportableModules.forEach { (mod, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedModules.contains(mod),
                            onCheckedChange = { checked ->
                                selectedModules = if (checked) selectedModules + mod else selectedModules - mod
                            }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (selectedModules.isEmpty()) {
                            viewModel.showMessage(strings.invalidInput)
                            return@OutlinedButton
                        }
                        scope.launch {
                            pendingExportJson = viewModel.generateBackupJson(selectedModules)
                            createBackupLauncher.launch("BASLUNE_OS_Partial_Backup.json")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.exportSelectedBackup)
                }
            }
        }

        item {
            BasluneCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                    Text(strings.localFirstSecurityTitle, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.localFirstSecurityDesc, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    pendingRestoreJson?.let { json ->
        ConfirmDestructiveDialog(
            title = strings.importAndRestore,
            message = strings.restoreConfirmMessage,
            confirmLabel = strings.restore,
            onConfirm = {
                viewModel.restoreFromBackup(
                    jsonText = json,
                    replaceExisting = true,
                    successMsg = strings.restoreSuccess,
                    errorMsg = strings.backupInvalidError
                )
                pendingRestoreJson = null
            },
            onDismiss = { pendingRestoreJson = null }
        )
    }
}
