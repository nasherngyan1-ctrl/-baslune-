package com.example.ui.screens.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.core.localization.LocalStrings
import com.example.data.local.TaskEntity
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.DateRangePreset
import com.example.domain.model.PriorityLevel
import com.example.domain.model.TaskStatus
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

enum class TaskFilterTab {
    MY_TASKS, TODAY, UPCOMING, OVERDUE, COMPLETED, BY_PRIORITY, KANBAN
}

@Composable
fun TasksScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    openAddImmediately: Boolean = false,
    onConsumeQuickAction: () -> Unit = {}
) {
    val strings = LocalStrings.current
    var activeTab by remember { mutableStateOf(TaskFilterTab.MY_TASKS) }
    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            editingTask = null
            showForm = true
            onConsumeQuickAction()
        }
    }

    val now = System.currentTimeMillis()
    val (todayStart, todayEnd) = remember(now) { FinancialCalculator.boundsFor(DateRangePreset.TODAY, nowMillis = now) }
    val projectsById = remember(state.projects) { state.projects.associateBy { it.id } }

    val filtered = remember(state.tasks, activeTab, now) {
        when (activeTab) {
            TaskFilterTab.MY_TASKS -> state.tasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
            TaskFilterTab.TODAY -> state.tasks.filter {
                it.status != TaskStatus.COMPLETED && it.dueDate != null && it.dueDate in todayStart..todayEnd
            }
            TaskFilterTab.UPCOMING -> state.tasks.filter {
                it.status != TaskStatus.COMPLETED && it.dueDate != null && it.dueDate > todayEnd
            }
            TaskFilterTab.OVERDUE -> state.tasks.filter {
                it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED && it.dueDate != null && it.dueDate < now
            }
            TaskFilterTab.COMPLETED -> state.tasks.filter { it.status == TaskStatus.COMPLETED }
            TaskFilterTab.BY_PRIORITY -> state.tasks.sortedByDescending { it.priority.ordinal }
            TaskFilterTab.KANBAN -> state.tasks
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showForm = true
                },
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Outlined.AddTask, contentDescription = strings.addTask)
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
                items = TaskFilterTab.entries,
                selected = activeTab,
                labelOf = {
                    when (it) {
                        TaskFilterTab.MY_TASKS -> strings.filterMyTasks
                        TaskFilterTab.TODAY -> strings.filterToday
                        TaskFilterTab.UPCOMING -> strings.filterUpcoming
                        TaskFilterTab.OVERDUE -> strings.filterOverdue
                        TaskFilterTab.COMPLETED -> strings.filterCompleted
                        TaskFilterTab.BY_PRIORITY -> strings.filterByPriority
                        TaskFilterTab.KANBAN -> strings.viewKanban
                    }
                },
                onSelect = { activeTab = it }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.CheckCircle,
                    title = strings.emptyTasksTitle,
                    description = strings.emptyTasksDesc,
                    actionLabel = strings.addTask,
                    onAction = {
                        editingTask = null
                        showForm = true
                    }
                )
            } else if (activeTab == TaskFilterTab.KANBAN) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS, TaskStatus.WAITING, TaskStatus.COMPLETED).forEach { st ->
                        val col = filtered.filter { it.status == st }
                        Column(modifier = Modifier.width(250.dp).fillMaxHeight()) {
                            StatusBadge("${strings.label(st)} (${col.size})", BadgeTone.DARK)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 88.dp)
                            ) {
                                items(col, key = { it.id }) { task ->
                                    TaskCard(
                                        task = task,
                                        projectName = task.projectId?.let { projectsById[it]?.name },
                                        now = now,
                                        onToggle = { viewModel.toggleTaskCompleted(task, it) },
                                        onEdit = {
                                            editingTask = task
                                            showForm = true
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
                    items(filtered, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            projectName = task.projectId?.let { projectsById[it]?.name },
                            now = now,
                            onToggle = { viewModel.toggleTaskCompleted(task, it) },
                            onEdit = {
                                editingTask = task
                                showForm = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showForm) {
        TaskFormDialog(
            initial = editingTask,
            state = state,
            onDismiss = { showForm = false },
            onSave = {
                viewModel.saveTask(it)
                showForm = false
            },
            onDelete = editingTask?.let { t ->
                {
                    viewModel.deleteTask(t.id)
                    showForm = false
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    projectName: String?,
    now: Long,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    val strings = LocalStrings.current
    val isOverdue = task.status != TaskStatus.COMPLETED &&
        task.status != TaskStatus.CANCELLED &&
        task.dueDate != null && task.dueDate < now

    BasluneCard(
        onClick = onEdit,
        borderColor = if (isOverdue) ErrorRed.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                val sub = listOfNotNull(
                    projectName,
                    task.dueDate?.let { Formatters.formatDate(it) }
                ).joinToString(" • ")
                if (sub.isNotBlank()) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(
                    text = strings.label(task.priority),
                    tone = when (task.priority) {
                        PriorityLevel.URGENT -> BadgeTone.ERROR
                        PriorityLevel.HIGH -> BadgeTone.WARNING
                        else -> BadgeTone.NEUTRAL
                    }
                )
                if (isOverdue) {
                    StatusBadge(strings.overdueWarning, BadgeTone.ERROR)
                }
                if (task.isRecurring) {
                    StatusBadge(strings.recurringDailyWeekly, BadgeTone.GOLD)
                }
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    initial: TaskEntity?,
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var projectId by remember { mutableStateOf(initial?.projectId) }
    var priority by remember { mutableStateOf(initial?.priority ?: PriorityLevel.NORMAL) }
    var status by remember { mutableStateOf(initial?.status ?: TaskStatus.NOT_STARTED) }
    var dueDateStr by remember { mutableStateOf(initial?.dueDate?.let { Formatters.formatDate(it) } ?: "") }
    var estHours by remember { mutableStateOf(if ((initial?.estimatedHours ?: 0.0) > 0) initial!!.estimatedHours.toString() else "") }
    var assignedTo by remember { mutableStateOf(initial?.assignedTo ?: state.settings.activeUserName) }
    var isRecurring by remember { mutableStateOf(initial?.isRecurring ?: false) }

    val activeProjects = state.projects.filter { !it.isArchived }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addTask else strings.editTask) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("${strings.taskTitle} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                    )
                }
                if (activeProjects.isNotEmpty()) {
                    item {
                        Text(strings.navProjects, style = MaterialTheme.typography.labelMedium)
                        FilterChipRow(
                            items = listOf(null) + activeProjects.map { it.id },
                            selected = projectId,
                            labelOf = { id ->
                                id?.let { pid -> activeProjects.firstOrNull { it.id == pid }?.name } ?: strings.none
                            },
                            onSelect = { projectId = it }
                        )
                    }
                }
                item {
                    Text(strings.priority, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = PriorityLevel.entries,
                        selected = priority,
                        labelOf = { strings.label(it) },
                        onSelect = { priority = it }
                    )
                }
                item {
                    Text(strings.status, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(
                        items = TaskStatus.entries,
                        selected = status,
                        labelOf = { strings.label(it) },
                        onSelect = { status = it }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dueDateStr,
                            onValueChange = { dueDateStr = it },
                            label = { Text("${strings.dueDate} (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = estHours,
                            onValueChange = { estHours = it },
                            label = { Text(strings.estimatedHours) },
                            singleLine = true,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = assignedTo,
                        onValueChange = { assignedTo = it },
                        label = { Text(strings.assignedTo) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                        Text(strings.recurringDailyWeekly, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(strings.description) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(strings.delete, color = ErrorRed)
                    }
                }
                Button(
                    onClick = {
                        if (title.isBlank()) return@Button
                        val linkedClientId = projectId?.let { pid -> activeProjects.firstOrNull { it.id == pid }?.clientId }
                        onSave(
                            (initial ?: TaskEntity(title = "")).copy(
                                title = title.trim(),
                                description = description.trim(),
                                projectId = projectId,
                                clientId = linkedClientId,
                                priority = priority,
                                status = status,
                                dueDate = Formatters.parseDateOrNull(dueDateStr),
                                estimatedHours = estHours.toDoubleOrNull() ?: 0.0,
                                assignedTo = assignedTo.trim(),
                                isRecurring = isRecurring
                            )
                        )
                    },
                    modifier = Modifier.testTag("save_task_button")
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
