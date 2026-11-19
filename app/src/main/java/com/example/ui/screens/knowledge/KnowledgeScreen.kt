package com.example.ui.screens.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalStrings
import com.example.data.local.KnowledgeArticleEntity
import com.example.domain.model.KnowledgeCategory
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun KnowledgeScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<KnowledgeCategory?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editingArticle by remember { mutableStateOf<KnowledgeArticleEntity?>(null) }

    val filtered = remember(state.knowledgeArticles, query, categoryFilter) {
        state.knowledgeArticles.filter { a ->
            val matchCat = categoryFilter == null || a.category == categoryFilter
            val q = query.trim().lowercase()
            val matchQ = q.isEmpty() || a.title.lowercase().contains(q) || a.content.lowercase().contains(q) || a.tags.lowercase().contains(q)
            matchCat && matchQ
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingArticle = null; showForm = true },
                modifier = Modifier.testTag("add_knowledge_fab")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = strings.addArticle)
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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChipRow(
                items = listOf(null) + KnowledgeCategory.entries,
                selected = categoryFilter,
                labelOf = { it?.let { c -> strings.label(c) } ?: strings.all },
                onSelect = { categoryFilter = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.MenuBook,
                    title = strings.emptyKnowledgeTitle,
                    description = strings.emptyKnowledgeDesc,
                    actionLabel = strings.addArticle,
                    onAction = { editingArticle = null; showForm = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtered, key = { it.id }) { article ->
                        BasluneCard(onClick = { editingArticle = article; showForm = true }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(article.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                SampleOrStarterBadge(article.isSample)
                            }
                            Text(
                                "${strings.label(article.category)} • ${Formatters.formatDate(article.updatedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(article.content, style = MaterialTheme.typography.bodySmall)
                            if (article.tags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                StatusBadge(article.tags, BadgeTone.GOLD)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        KnowledgeFormDialog(
            initial = editingArticle,
            onDismiss = { showForm = false },
            onSave = { viewModel.saveKnowledgeArticle(it); showForm = false },
            onDelete = editingArticle?.let { a -> { viewModel.deleteKnowledgeArticle(a.id); showForm = false } }
        )
    }
}

@Composable
private fun KnowledgeFormDialog(
    initial: KnowledgeArticleEntity?,
    onDismiss: () -> Unit,
    onSave: (KnowledgeArticleEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: KnowledgeCategory.PROCEDURES) }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addArticle) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("${strings.articleTitle} *") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text(strings.category, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = KnowledgeCategory.entries, selected = category, labelOf = { strings.label(it) }, onSelect = { category = it })
                }
                item { OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("${strings.articleContent} *") }, minLines = 4, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text(strings.tags) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(strings.delete, color = ErrorRed) }
                Button(onClick = {
                    if (title.isBlank() || content.isBlank()) return@Button
                    onSave(
                        (initial ?: KnowledgeArticleEntity(title = "", content = "")).copy(
                            title = title.trim(),
                            category = category,
                            content = content.trim(),
                            tags = tags.trim()
                        )
                    )
                }) { Text(strings.save) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
