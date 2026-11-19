package com.example.ui.screens.documents

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalStrings
import com.example.core.security.InputValidator
import com.example.data.local.DocumentEntity
import com.example.domain.model.DocumentCategory
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun DocumentsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    var categoryFilter by remember { mutableStateOf<DocumentCategory?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editingDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    val filtered = remember(state.documents, categoryFilter) {
        if (categoryFilter == null) state.documents else state.documents.filter { it.category == categoryFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingDoc = null; showForm = true },
                modifier = Modifier.testTag("add_document_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.addDocument)
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
                Text(strings.driveStorageNotice, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilterChipRow(
                items = listOf(null) + DocumentCategory.entries,
                selected = categoryFilter,
                labelOf = { it?.let { c -> strings.label(c) } ?: strings.all },
                onSelect = { categoryFilter = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.FolderOpen,
                    title = strings.emptyDocumentsTitle,
                    description = strings.emptyDocumentsDesc,
                    actionLabel = strings.addDocument,
                    onAction = { editingDoc = null; showForm = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtered, key = { it.id }) { doc ->
                        BasluneCard(onClick = { editingDoc = doc; showForm = true }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(doc.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${strings.label(doc.category)} • ${Formatters.formatDate(doc.dateAdded)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (InputValidator.isValidWebUrl(doc.driveUrl)) {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(doc.driveUrl)))
                                            }.onFailure { viewModel.showMessage(strings.invalidUrl) }
                                        } else {
                                            viewModel.showMessage(strings.invalidUrl)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Outlined.OpenInNew, contentDescription = strings.openLink)
                                }
                            }
                            if (doc.description.isNotBlank()) {
                                Text(doc.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        DocumentFormDialog(
            initial = editingDoc,
            state = state,
            onDismiss = { showForm = false },
            onSave = { viewModel.saveDocument(it); showForm = false },
            onDelete = editingDoc?.let { d -> { viewModel.deleteDocument(d.id); showForm = false } }
        )
    }
}

@Composable
private fun DocumentFormDialog(
    initial: DocumentEntity?,
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (DocumentEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: DocumentCategory.PROJECT_FILES) }
    var driveUrl by remember { mutableStateOf(initial?.driveUrl ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }
    var clientId by remember { mutableStateOf(initial?.clientId) }
    var projectId by remember { mutableStateOf(initial?.projectId) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addDocument) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("${strings.documentTitle} *") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = driveUrl, onValueChange = { driveUrl = it }, label = { Text("${strings.driveUrl} *") }, placeholder = { Text(strings.driveUrlHint) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text(strings.category, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = DocumentCategory.entries, selected = category, labelOf = { strings.label(it) }, onSelect = { category = it })
                }
                if (state.clients.isNotEmpty()) {
                    item {
                        Text(strings.client, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + state.clients.map { it.id },
                            selected = clientId,
                            labelOf = { id -> id?.let { cid -> state.clients.firstOrNull { it.id == cid }?.fullName } ?: strings.none },
                            onSelect = { clientId = it }
                        )
                    }
                }
                if (state.projects.isNotEmpty()) {
                    item {
                        Text(strings.navProjects, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + state.projects.map { it.id },
                            selected = projectId,
                            labelOf = { id -> id?.let { pid -> state.projects.firstOrNull { it.id == pid }?.name } ?: strings.none },
                            onSelect = { projectId = it }
                        )
                    }
                }
                item { OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text(strings.tags) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(strings.description) }, modifier = Modifier.fillMaxWidth()) }
                if (errorMsg != null) item { Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(strings.delete, color = ErrorRed) }
                Button(onClick = {
                    if (title.isBlank() || !InputValidator.isValidWebUrl(driveUrl)) {
                        errorMsg = strings.invalidUrl
                        return@Button
                    }
                    onSave(
                        (initial ?: DocumentEntity(title = "", driveUrl = "")).copy(
                            title = title.trim(),
                            category = category,
                            driveUrl = driveUrl.trim(),
                            clientId = clientId,
                            projectId = projectId,
                            description = description.trim(),
                            tags = tags.trim()
                        )
                    )
                }) { Text(strings.save) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
