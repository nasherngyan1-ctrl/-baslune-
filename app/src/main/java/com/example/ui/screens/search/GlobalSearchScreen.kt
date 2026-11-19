package com.example.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.BasluneCard
import com.example.core.design.BadgeTone
import com.example.core.design.EmptyStateView
import com.example.core.design.FilterChipRow
import com.example.core.design.StatusBadge
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.domain.model.AppModule
import com.example.ui.navigation.AppRoutes
import com.example.ui.viewmodel.BasluneUiState

data class SearchHit(
    val id: String,
    val moduleLabel: String,
    val title: String,
    val subtitle: String,
    val route: String
)

@Composable
fun GlobalSearchScreen(
    state: BasluneUiState,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    var filterModule by remember { mutableStateOf<String?>(null) }

    val hits = remember(query, filterModule, state) {
        val q = query.trim().lowercase()
        val list = mutableListOf<SearchHit>()

        if (state.canAccess(AppModule.CLIENTS)) {
            state.clients.filter {
                q.isEmpty() || it.fullName.lowercase().contains(q) || it.companyName.lowercase().contains(q) || it.city.lowercase().contains(q)
            }.forEach {
                list += SearchHit("cl_${it.id}", strings.navClients, it.fullName, "${it.city} • ${strings.label(it.status)}", AppRoutes.clientDetail(it.id))
            }
        }
        if (state.canAccess(AppModule.PROJECTS)) {
            state.projects.filter {
                q.isEmpty() || it.name.lowercase().contains(q) || it.projectCode.lowercase().contains(q)
            }.forEach {
                list += SearchHit("prj_${it.id}", strings.navProjects, it.name, "${it.projectCode} • ${strings.label(it.status)}", AppRoutes.projectDetail(it.id))
            }
        }
        if (state.canAccess(AppModule.TASKS)) {
            state.tasks.filter {
                q.isEmpty() || it.title.lowercase().contains(q) || it.description.lowercase().contains(q)
            }.forEach {
                list += SearchHit("tsk_${it.id}", strings.navTasks, it.title, strings.label(it.status), AppRoutes.TASKS)
            }
        }
        if (state.canAccess(AppModule.INVOICES)) {
            state.invoices.filter {
                q.isEmpty() || it.invoiceNumber.lowercase().contains(q) || it.clientNameSnapshot.lowercase().contains(q)
            }.forEach {
                list += SearchHit("inv_${it.id}", strings.navInvoices, it.invoiceNumber, "${it.clientNameSnapshot} • ${Formatters.formatMoney(it.total, it.currency, lang)}", AppRoutes.invoiceDetail(it.id))
            }
        }
        if (state.canAccess(AppModule.FINANCE)) {
            state.transactions.filter {
                q.isEmpty() || it.description.lowercase().contains(q) || it.transactionCode.lowercase().contains(q)
            }.forEach {
                list += SearchHit("tx_${it.id}", strings.navFinance, it.description.ifBlank { it.transactionCode }, Formatters.formatMoney(it.amount, it.currency, lang), AppRoutes.FINANCE)
            }
        }
        if (state.canAccess(AppModule.DOCUMENTS)) {
            state.documents.filter {
                q.isEmpty() || it.title.lowercase().contains(q) || it.tags.lowercase().contains(q)
            }.forEach {
                list += SearchHit("doc_${it.id}", strings.navDocuments, it.title, strings.label(it.category), AppRoutes.DOCUMENTS)
            }
        }
        if (state.canAccess(AppModule.KNOWLEDGE)) {
            state.knowledgeArticles.filter {
                q.isEmpty() || it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
            }.forEach {
                list += SearchHit("knw_${it.id}", strings.navKnowledge, it.title, strings.label(it.category), AppRoutes.KNOWLEDGE)
            }
        }

        if (filterModule == null) list else list.filter { it.moduleLabel == filterModule }
    }

    val moduleFilters = listOf(null, strings.navClients, strings.navProjects, strings.navTasks, strings.navInvoices, strings.navFinance, strings.navDocuments, strings.navKnowledge)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("global_search_screen")
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.searchPlaceholder) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("global_search_input")
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilterChipRow(
            items = moduleFilters,
            selected = filterModule,
            labelOf = { it ?: strings.all },
            onSelect = { filterModule = it }
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (hits.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Search,
                title = strings.search,
                description = strings.noDataForRange
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hits, key = { it.id }) { hit ->
                    BasluneCard(onClick = { onNavigate(hit.route) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hit.title, style = MaterialTheme.typography.titleMedium)
                                Text(hit.subtitle, style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(hit.moduleLabel, BadgeTone.GOLD)
                        }
                    }
                }
            }
        }
    }
}
