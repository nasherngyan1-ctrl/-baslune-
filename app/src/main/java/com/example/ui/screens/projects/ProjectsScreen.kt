package com.example.ui.screens.projects

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.design.*
import com.example.core.localization.Formatters
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.core.security.BusinessPermission
import com.example.core.security.InputValidator
import com.example.core.security.RolePermissions
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectMilestoneEntity
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

enum class ProjectViewMode { LIST, CARDS, KANBAN, CALENDAR }

@Composable
fun ProjectsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    openAddImmediately: Boolean = false,
    onConsumeQuickAction: () -> Unit = {},
    onOpenProject: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val canEdit = remember(state.settings.activeRole) {
        RolePermissions.hasPermission(state.settings.activeRole, BusinessPermission.EDIT_PROJECTS)
    }
    val canDelete = remember(state.settings.activeRole) {
        RolePermissions.hasPermission(state.settings.activeRole, BusinessPermission.ARCHIVE_PROJECTS)
    }

    var viewMode by remember { mutableStateOf(ProjectViewMode.CARDS) }
    var statusFilter by remember { mutableStateOf<ProjectStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<ProjectEntity?>(null) }
    var scrollExpectedNewTop by remember { mutableStateOf(false) }
    var previousTopId by remember { mutableStateOf<Long?>(null) }
    val cardsListState = rememberLazyListState()
    val cardsGridState = rememberLazyGridState()
    val listModeState = rememberLazyListState()

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            if (canEdit) {
                editingProject = null
                showForm = true
            }
            onConsumeQuickAction()
        }
    }

    val clientsById = remember(state.clients) { state.clients.associateBy { it.id } }
    val filtered = remember(state.projects, statusFilter, searchQuery, clientsById) {
        state.projects.filter { p ->
            val matchStatus = if (statusFilter == null) !p.isArchived else p.status == statusFilter
            val q = searchQuery.trim().lowercase()
            val clientName = p.clientId?.let { clientsById[it]?.fullName.orEmpty() }.orEmpty().lowercase()
            val matchQ = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.projectCode.lowercase().contains(q) ||
                p.description.lowercase().contains(q) ||
                clientName.contains(q)
            matchStatus && matchQ
        }
    }

    LaunchedEffect(filtered.firstOrNull()?.id, scrollExpectedNewTop) {
        val currentTopId = filtered.firstOrNull()?.id
        if (scrollExpectedNewTop && currentTopId != null && currentTopId != previousTopId) {
            cardsListState.scrollToItem(0)
            cardsGridState.scrollToItem(0)
            listModeState.scrollToItem(0)
            scrollExpectedNewTop = false
        }
    }

    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = {
                        editingProject = null
                        showForm = true
                    },
                    modifier = Modifier.testTag("add_project_fab")
                ) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = strings.addProject)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("projects_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.search) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_search_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChipRow(
                items = ProjectViewMode.entries,
                selected = viewMode,
                labelOf = {
                    when (it) {
                        ProjectViewMode.LIST -> strings.viewList
                        ProjectViewMode.CARDS -> strings.viewCards
                        ProjectViewMode.KANBAN -> strings.viewKanban
                        ProjectViewMode.CALENDAR -> strings.viewCalendar
                    }
                },
                onSelect = { viewMode = it }
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipRow(
                items = listOf(null) + ProjectStatus.entries,
                selected = statusFilter,
                labelOf = { it?.let { s -> strings.label(s) } ?: strings.all },
                onSelect = { statusFilter = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.WorkOutline,
                    title = strings.emptyProjectsTitle,
                    description = strings.emptyProjectsDesc,
                    actionLabel = if (canEdit) strings.addProject else null,
                    onAction = if (canEdit) {
                        {
                            editingProject = null
                            showForm = true
                        }
                    } else null
                )
            } else {
                when (viewMode) {
                    ProjectViewMode.KANBAN -> {
                        val kanbanCols = listOf(
                            ProjectStatus.IDEA,
                            ProjectStatus.NEW_REQUEST,
                            ProjectStatus.PLANNING,
                            ProjectStatus.IN_PROGRESS,
                            ProjectStatus.WAITING_FOR_CLIENT,
                            ProjectStatus.UNDER_REVIEW,
                            ProjectStatus.REVISION,
                            ProjectStatus.COMPLETED
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            kanbanCols.forEach { st ->
                                val colItems = filtered.filter { it.status == st }
                                Column(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .fillMaxHeight()
                                ) {
                                    StatusBadge("${strings.label(st)} (${colItems.size})", BadgeTone.DARK)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(bottom = 88.dp)
                                    ) {
                                        items(colItems, key = { it.id }) { p ->
                                            ProjectCardItem(
                                                project = p,
                                                clientName = p.clientId?.let { clientsById[it]?.fullName },
                                                compact = true,
                                                canEdit = canEdit,
                                                canDelete = canDelete,
                                                onClick = { onOpenProject(p.id) },
                                                onEdit = {
                                                    editingProject = p
                                                    showForm = true
                                                },
                                                onDelete = { deleteCandidate = p }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ProjectViewMode.CALENDAR -> {
                        val withDeadlines = filtered.sortedBy { it.deadline ?: Long.MAX_VALUE }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(withDeadlines, key = { it.id }) { p ->
                                BasluneCard(
                                    onClick = { onOpenProject(p.id) },
                                    modifier = Modifier.testTag("project_calendar_card_${p.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = p.deadline?.let { Formatters.formatDate(it) } ?: "—",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                                            p.clientId?.let { cid -> clientsById[cid]?.fullName }?.let { cName ->
                                                Text(
                                                    text = cName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            StatusBadge(strings.label(p.status), BadgeTone.GOLD)
                                            if (canEdit) {
                                                IconButton(
                                                    onClick = {
                                                        editingProject = p
                                                        showForm = true
                                                    },
                                                    modifier = Modifier.testTag("edit_project_btn_${p.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.Edit,
                                                        contentDescription = "${strings.edit} ${p.name}"
                                                    )
                                                }
                                            }
                                            if (canDelete) {
                                                IconButton(
                                                    onClick = { deleteCandidate = p },
                                                    modifier = Modifier.testTag("delete_project_btn_${p.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.DeleteOutline,
                                                        contentDescription = "${strings.delete} ${p.name}",
                                                        tint = ErrorRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ProjectViewMode.LIST -> {
                        LazyColumn(
                            state = listModeState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(filtered, key = { it.id }) { p ->
                                ProjectCardItem(
                                    project = p,
                                    clientName = p.clientId?.let { clientsById[it]?.fullName },
                                    compact = true,
                                    canEdit = canEdit,
                                    canDelete = canDelete,
                                    onClick = { onOpenProject(p.id) },
                                    onEdit = {
                                        editingProject = p
                                        showForm = true
                                    },
                                    onDelete = { deleteCandidate = p }
                                )
                            }
                        }
                    }

                    ProjectViewMode.CARDS -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            if (maxWidth >= 680.dp) {
                                LazyVerticalGrid(
                                    state = cardsGridState,
                                    columns = GridCells.Adaptive(minSize = 330.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 88.dp)
                                ) {
                                    gridItems(filtered, key = { it.id }) { p ->
                                        ProjectCardItem(
                                            project = p,
                                            clientName = p.clientId?.let { clientsById[it]?.fullName },
                                            compact = false,
                                            canEdit = canEdit,
                                            canDelete = canDelete,
                                            onClick = { onOpenProject(p.id) },
                                            onEdit = {
                                                editingProject = p
                                                showForm = true
                                            },
                                            onDelete = { deleteCandidate = p }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = cardsListState,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 88.dp)
                                ) {
                                    items(filtered, key = { it.id }) { p ->
                                        ProjectCardItem(
                                            project = p,
                                            clientName = p.clientId?.let { clientsById[it]?.fullName },
                                            compact = false,
                                            canEdit = canEdit,
                                            canDelete = canDelete,
                                            onClick = { onOpenProject(p.id) },
                                            onEdit = {
                                                editingProject = p
                                                showForm = true
                                            },
                                            onDelete = { deleteCandidate = p }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        ProjectFormDialog(
            initial = editingProject,
            state = state,
            onDismiss = {
                showForm = false
                editingProject = null
            },
            onSave = { updated ->
                if (editingProject == null) {
                    previousTopId = filtered.firstOrNull()?.id
                    scrollExpectedNewTop = true
                }
                viewModel.saveProject(updated, strings.copiedOrSaved)
                showForm = false
                editingProject = null
            }
        )
    }

    deleteCandidate?.let { project ->
        ConfirmDestructiveDialog(
            title = strings.delete,
            message = "${project.name}\n\n${strings.deleteProjectConfirm}",
            confirmLabel = strings.delete,
            onConfirm = {
                viewModel.deleteProject(project.id, strings.copiedOrSaved)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }
}

@Composable
private fun ProjectCardItem(
    project: ProjectEntity,
    clientName: String?,
    compact: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    BasluneCard(
        onClick = onClick,
        modifier = Modifier.testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(clientName, strings.label(project.category)).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusBadge(
                    text = strings.label(project.status),
                    tone = projectStatusTone(project.status)
                )
                SampleOrStarterBadge(project.isSample, project.isStarter)
            }
        }

        if (!compact) {
            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (project.classification.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.classification,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = strings.label(project.priority),
                    tone = priorityTone(project.priority)
                )
                project.deadline?.let {
                    Text(
                        text = "${strings.deadline}: ${Formatters.formatDate(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${strings.progress}: ${project.progressPercent}%", style = MaterialTheme.typography.labelMedium)
                val displayMoney = when {
                    project.agreedPrice > 0.0 -> Formatters.formatMoney(project.agreedPrice, project.currency, lang)
                    project.budget > 0.0 -> "${strings.budget}: ${Formatters.formatMoney(project.budget, project.currency, lang)}"
                    else -> null
                }
                if (displayMoney != null) {
                    Text(displayMoney, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (canEdit || canDelete) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canEdit) {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_project_btn_${project.id}")
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.edit)
                    }
                }
                if (canDelete) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_project_btn_${project.id}")
                    ) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ErrorRed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.delete, color = ErrorRed)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val project = state.projects.firstOrNull { it.id == projectId } ?: return
    val client = project.clientId?.let { cid -> state.clients.firstOrNull { it.id == cid } }
    val milestones = remember(state.milestones, projectId) { state.milestones.filter { it.projectId == projectId } }
    val tasks = remember(state.tasks, projectId) { state.tasks.filter { it.projectId == projectId } }
    val profitability = remember(state.transactions, projectId) {
        FinancialCalculator.calculateProjectProfitability(projectId, state.transactions)
    }
    val logs = remember(state.activityLogs, projectId) {
        state.activityLogs.filter { it.entityType == "PROJECT" && it.entityId == projectId }
    }

    val canEdit = remember(state.settings.activeRole) {
        RolePermissions.hasPermission(state.settings.activeRole, BusinessPermission.EDIT_PROJECTS)
    }
    val canDelete = remember(state.settings.activeRole) {
        RolePermissions.hasPermission(state.settings.activeRole, BusinessPermission.ARCHIVE_PROJECTS)
    }

    var showEdit by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newMilestoneTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("project_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BasluneCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(project.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${project.projectCode} • ${strings.label(project.category)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusBadge(strings.label(project.status), projectStatusTone(project.status))
                        StatusBadge(strings.label(project.priority), priorityTone(project.priority))
                    }
                }
                if (client != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${strings.client}: ${client.fullName}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${strings.startDate}: ${Formatters.formatDate(project.startDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strings.deadline}: ${project.deadline?.let { Formatters.formatDate(it) } ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (project.assignedMembers.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.assignedTo}: ${project.assignedMembers}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (project.classification.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${strings.classificationLabel}: ${project.classification}", style = MaterialTheme.typography.bodySmall)
                }
                if (project.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(project.description, style = MaterialTheme.typography.bodyMedium)
                }
                if (project.deliverables.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${strings.deliverables}: ${project.deliverables}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (project.internalNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${strings.notes}: ${project.internalNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (project.externalUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (InputValidator.isValidWebUrl(project.externalUrl)) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(project.externalUrl)))
                                }.onFailure { viewModel.showMessage(strings.invalidUrl) }
                            } else {
                                viewModel.showMessage(strings.invalidUrl)
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.openLink)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("${strings.progress}: ${project.progressPercent}%", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (canEdit || canDelete) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canEdit) {
                            OutlinedButton(
                                onClick = { showEdit = true },
                                modifier = Modifier.testTag("detail_edit_project_btn")
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.edit)
                            }
                            OutlinedButton(
                                onClick = { viewModel.duplicateProject(project) },
                                modifier = Modifier.testTag("detail_duplicate_project_btn")
                            ) {
                                Text(strings.duplicate)
                            }
                        }
                        if (canDelete && !project.isArchived) {
                            OutlinedButton(
                                onClick = { showArchiveConfirm = true },
                                modifier = Modifier.testTag("detail_archive_project_btn")
                            ) {
                                Text(strings.archive)
                            }
                        }
                        if (canDelete) {
                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.testTag("detail_delete_project_btn")
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ErrorRed
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.delete, color = ErrorRed)
                            }
                        }
                    }
                }
            }
        }

        // Financial Profitability (computed strictly from actual transactions!)
        item {
            BasluneCard {
                Text(strings.profitability, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(strings.agreedPrice, style = MaterialTheme.typography.labelSmall)
                        Text(
                            Formatters.formatMoney(project.agreedPrice, project.currency, lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(strings.budget, style = MaterialTheme.typography.labelSmall)
                        Text(
                            Formatters.formatMoney(project.budget, project.currency, lang),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column {
                        Text(strings.collectedIncome, style = MaterialTheme.typography.labelSmall)
                        Text(
                            Formatters.formatMoney(profitability.incomeIqd, CurrencyCode.IQD, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                        Text(
                            Formatters.formatMoney(profitability.incomeUsd, CurrencyCode.USD, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                    Column {
                        Text(strings.actualExpenses, style = MaterialTheme.typography.labelSmall)
                        Text(
                            Formatters.formatMoney(profitability.expenseIqd, CurrencyCode.IQD, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                        Text(
                            Formatters.formatMoney(profitability.expenseUsd, CurrencyCode.USD, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }
                }
            }
        }

        // Milestones
        item {
            BasluneCard {
                Text(strings.milestones, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                milestones.forEach { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = m.isCompleted,
                            enabled = canEdit,
                            onCheckedChange = { viewModel.toggleMilestone(m) }
                        )
                        Text(m.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (canEdit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMilestoneTitle,
                            onValueChange = { newMilestoneTitle = it },
                            placeholder = { Text(strings.addMilestone) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (newMilestoneTitle.isNotBlank()) {
                                    viewModel.addMilestone(
                                        ProjectMilestoneEntity(
                                            projectId = projectId,
                                            title = newMilestoneTitle.trim(),
                                            isSample = project.isSample
                                        )
                                    )
                                    newMilestoneTitle = ""
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = strings.addMilestone)
                        }
                    }
                }
            }
        }

        // Tasks inside project
        item {
            BasluneCard {
                Text("${strings.navTasks} (${tasks.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (tasks.isEmpty()) {
                    Text(strings.emptyTasksTitle, style = MaterialTheme.typography.bodySmall)
                } else {
                    tasks.forEach { t ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = t.status == TaskStatus.COMPLETED,
                                onCheckedChange = { viewModel.toggleTaskCompleted(t, it) }
                            )
                            Text(t.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Activity Log
        if (logs.isNotEmpty()) {
            item {
                BasluneCard {
                    Text(strings.activityLog, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    logs.take(8).forEach { log ->
                        Text(
                            text = "${Formatters.formatDateTime(log.timestamp)} • ${log.action}: ${log.details}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showEdit) {
        ProjectFormDialog(
            initial = project,
            state = state,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                viewModel.saveProject(updated, strings.copiedOrSaved)
                showEdit = false
            }
        )
    }

    if (showArchiveConfirm) {
        ConfirmDestructiveDialog(
            title = strings.archive,
            message = project.name,
            confirmLabel = strings.archive,
            onConfirm = {
                viewModel.archiveProject(project)
                showArchiveConfirm = false
                onBack()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDestructiveDialog(
            title = strings.delete,
            message = "${project.name}\n\n${strings.deleteProjectConfirm}",
            confirmLabel = strings.delete,
            onConfirm = {
                viewModel.deleteProject(project.id, strings.copiedOrSaved)
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
fun ProjectFormDialog(
    initial: ProjectEntity?,
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit
) {
    val strings = LocalStrings.current
    val formKey = initial?.id ?: -1L
    var name by remember(formKey) { mutableStateOf(initial?.name ?: "") }
    var description by remember(formKey) { mutableStateOf(initial?.description ?: "") }
    var selectedClientId by remember(formKey) { mutableStateOf(initial?.clientId) }
    var category by remember(formKey) { mutableStateOf(initial?.category ?: ProjectCategory.WEBSITE_DEVELOPMENT) }
    var status by remember(formKey) { mutableStateOf(initial?.status ?: ProjectStatus.PLANNING) }
    var priority by remember(formKey) { mutableStateOf(initial?.priority ?: PriorityLevel.NORMAL) }
    var currency by remember(formKey) { mutableStateOf(initial?.currency ?: state.settings.defaultCurrency) }
    var startDateStr by remember(formKey) {
        mutableStateOf(Formatters.formatDate(initial?.startDate ?: System.currentTimeMillis()))
    }
    var deadlineStr by remember(formKey) {
        mutableStateOf(initial?.deadline?.let { Formatters.formatDate(it) } ?: "")
    }
    var budgetStr by remember(formKey) {
        mutableStateOf(formatEditableNumber(initial?.budget ?: 0.0))
    }
    var agreedPriceStr by remember(formKey) {
        mutableStateOf(formatEditableNumber(initial?.agreedPrice ?: 0.0))
    }
    var progressText by remember(formKey) {
        mutableStateOf((initial?.progressPercent ?: 0).toString())
    }
    var assignedMembers by remember(formKey) { mutableStateOf(initial?.assignedMembers ?: "") }
    var deliverables by remember(formKey) { mutableStateOf(initial?.deliverables ?: "") }
    var externalUrl by remember(formKey) { mutableStateOf(initial?.externalUrl ?: "") }
    var classification by remember(formKey) { mutableStateOf(initial?.classification ?: "") }
    var internalNotes by remember(formKey) { mutableStateOf(initial?.internalNotes ?: "") }
    var errorMsg by remember(formKey) { mutableStateOf<String?>(null) }

    val availableClients = remember(state.clients, initial?.clientId) {
        state.clients.filter { !it.isArchived || it.id == initial?.clientId }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(min = 300.dp, max = 640.dp)
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .imePadding()
                .testTag("project_form_dialog"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Fixed Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial == null) strings.addProject else strings.editProject,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_project_dialog_button")
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = strings.close)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Scrollable Form Body
                BoxWithConstraints(modifier = Modifier.weight(1f, fill = false)) {
                    val isWideForm = maxWidth >= 460.dp
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (errorMsg != null) errorMsg = null
                            },
                            label = { Text("${strings.projectName} *") },
                            isError = errorMsg != null && name.isBlank(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_name_input")
                        )
                        Text(strings.client, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + availableClients.map { it.id },
                            selected = selectedClientId,
                            labelOf = { id ->
                                id?.let { cid -> availableClients.firstOrNull { it.id == cid }?.fullName } ?: strings.none
                            },
                            onSelect = { selectedClientId = it },
                            modifier = Modifier.testTag("project_client_selector")
                        )
                        Text(strings.category, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = ProjectCategory.entries,
                            selected = category,
                            labelOf = { strings.label(it) },
                            onSelect = { category = it }
                        )
                        Text(strings.status, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = ProjectStatus.entries,
                            selected = status,
                            labelOf = { strings.label(it) },
                            onSelect = { status = it },
                            modifier = Modifier.testTag("project_status_selector")
                        )
                        Text(strings.priority, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = PriorityLevel.entries,
                            selected = priority,
                            labelOf = { strings.label(it) },
                            onSelect = { priority = it },
                            modifier = Modifier.testTag("project_priority_selector")
                        )
                        if (isWideForm) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = startDateStr,
                                    onValueChange = {
                                        startDateStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text("${strings.startDate} (YYYY-MM-DD)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("project_start_date_input")
                                )
                                OutlinedTextField(
                                    value = deadlineStr,
                                    onValueChange = {
                                        deadlineStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text("${strings.deadline} (YYYY-MM-DD)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("project_deadline_input")
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = startDateStr,
                                    onValueChange = {
                                        startDateStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text("${strings.startDate} (YYYY-MM-DD)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("project_start_date_input")
                                )
                                OutlinedTextField(
                                    value = deadlineStr,
                                    onValueChange = {
                                        deadlineStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text("${strings.deadline} (YYYY-MM-DD)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("project_deadline_input")
                                )
                            }
                        }
                        if (isWideForm) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = budgetStr,
                                    onValueChange = {
                                        budgetStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text(strings.budget) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("project_budget_input")
                                )
                                OutlinedTextField(
                                    value = agreedPriceStr,
                                    onValueChange = {
                                        agreedPriceStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text(strings.agreedPrice) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("project_agreed_price_input")
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = budgetStr,
                                    onValueChange = {
                                        budgetStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text(strings.budget) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("project_budget_input")
                                )
                                OutlinedTextField(
                                    value = agreedPriceStr,
                                    onValueChange = {
                                        agreedPriceStr = it
                                        if (errorMsg != null) errorMsg = null
                                    },
                                    label = { Text(strings.agreedPrice) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("project_agreed_price_input")
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.currency, style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                FilterChipRow(
                                    items = CurrencyCode.entries,
                                    selected = currency,
                                    labelOf = { it.name },
                                    onSelect = { currency = it }
                                )
                            }
                            OutlinedTextField(
                                value = progressText,
                                onValueChange = {
                                    progressText = it
                                    if (errorMsg != null) errorMsg = null
                                },
                                label = { Text("${strings.progress} %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .width(120.dp)
                                    .testTag("project_progress_input")
                            )
                        }
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(strings.description) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_description_input")
                        )
                        OutlinedTextField(
                            value = assignedMembers,
                            onValueChange = { assignedMembers = it },
                            label = { Text(strings.assignedTo) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_assigned_members_input")
                        )
                        OutlinedTextField(
                            value = deliverables,
                            onValueChange = { deliverables = it },
                            label = { Text(strings.deliverables) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_deliverables_input")
                        )
                        OutlinedTextField(
                            value = externalUrl,
                            onValueChange = {
                                externalUrl = it
                                if (errorMsg != null) errorMsg = null
                            },
                            label = { Text(strings.externalUrl) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_external_url_input")
                        )
                        OutlinedTextField(
                            value = classification,
                            onValueChange = { classification = it },
                            label = { Text(strings.classificationLabel) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_classification_input")
                        )
                        OutlinedTextField(
                            value = internalNotes,
                            onValueChange = { internalNotes = it },
                            label = { Text(strings.notes) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_notes_input")
                        )
                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag("project_form_error")
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Fixed Dialog Footer (Always visible on mobile & desktop)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_project_button")
                    ) {
                        Text(strings.cancel)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = strings.invalidInput
                                return@Button
                            }
                            val parsedStart = if (startDateStr.isBlank()) {
                                initial?.startDate ?: System.currentTimeMillis()
                            } else {
                                Formatters.parseDateOrNull(startDateStr) ?: run {
                                    errorMsg = "${strings.startDate}: YYYY-MM-DD"
                                    return@Button
                                }
                            }
                            val parsedDeadline = if (deadlineStr.isBlank()) {
                                null
                            } else {
                                Formatters.parseDateOrNull(deadlineStr) ?: run {
                                    errorMsg = "${strings.deadline}: YYYY-MM-DD"
                                    return@Button
                                }
                            }
                            if (parsedDeadline != null && parsedDeadline < parsedStart) {
                                errorMsg = strings.invalidInput
                                return@Button
                            }
                            val parsedBudget = parseNonNegativeNumber(budgetStr) ?: run {
                                errorMsg = "${strings.budget}: ${strings.invalidInput}"
                                return@Button
                            }
                            val parsedAgreedPrice = parseNonNegativeNumber(agreedPriceStr) ?: run {
                                errorMsg = "${strings.agreedPrice}: ${strings.invalidInput}"
                                return@Button
                            }
                            val parsedProgress = if (progressText.isBlank()) {
                                0
                            } else {
                                progressText.trim().toIntOrNull()?.coerceIn(0, 100) ?: run {
                                    errorMsg = "${strings.progress}: 0 - 100"
                                    return@Button
                                }
                            }
                            val trimmedUrl = externalUrl.trim()
                            if (trimmedUrl.isNotEmpty() && !InputValidator.isValidWebUrl(trimmedUrl)) {
                                errorMsg = strings.invalidUrl
                                return@Button
                            }

                            onSave(
                                (initial ?: ProjectEntity(name = "")).copy(
                                    name = name.trim(),
                                    clientId = selectedClientId,
                                    description = description.trim(),
                                    category = category,
                                    startDate = parsedStart,
                                    deadline = parsedDeadline,
                                    budget = parsedBudget,
                                    agreedPrice = parsedAgreedPrice,
                                    currency = currency,
                                    status = status,
                                    priority = priority,
                                    assignedMembers = assignedMembers.trim(),
                                    progressPercent = parsedProgress,
                                    deliverables = deliverables.trim(),
                                    externalUrl = trimmedUrl,
                                    classification = classification.trim(),
                                    internalNotes = internalNotes.trim(),
                                    isArchived = status == ProjectStatus.ARCHIVED
                                )
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("save_project_button")
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}

private fun projectStatusTone(status: ProjectStatus): BadgeTone = when (status) {
    ProjectStatus.COMPLETED -> BadgeTone.SUCCESS
    ProjectStatus.IN_PROGRESS -> BadgeTone.GOLD
    ProjectStatus.WAITING_FOR_CLIENT, ProjectStatus.REVISION -> BadgeTone.WARNING
    ProjectStatus.CANCELLED -> BadgeTone.ERROR
    else -> BadgeTone.NEUTRAL
}

private fun priorityTone(priority: PriorityLevel): BadgeTone = when (priority) {
    PriorityLevel.URGENT -> BadgeTone.ERROR
    PriorityLevel.HIGH -> BadgeTone.WARNING
    PriorityLevel.NORMAL -> BadgeTone.GOLD
    PriorityLevel.LOW -> BadgeTone.NEUTRAL
}

private fun formatEditableNumber(value: Double): String {
    if (value <= 0.0) return ""
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

private fun parseNonNegativeNumber(raw: String): Double? {
    val cleaned = raw.trim().replace(",", "")
    if (cleaned.isEmpty()) return 0.0
    val parsed = cleaned.toDoubleOrNull() ?: return null
    return if (parsed.isFinite() && parsed >= 0.0) parsed else null
}
