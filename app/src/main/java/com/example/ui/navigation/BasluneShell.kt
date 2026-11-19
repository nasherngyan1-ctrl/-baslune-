package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.design.BadgeTone
import com.example.core.design.StatusBadge
import com.example.core.localization.LocalStrings
import com.example.domain.model.AppModule
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.Ivory
import com.example.ui.theme.RichBrown
import com.example.ui.viewmodel.BasluneUiState
import kotlinx.coroutines.launch

data class NavModuleItem(
    val module: AppModule,
    val route: String,
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector
)

@Composable
fun rememberModuleItems(state: BasluneUiState): List<NavModuleItem> {
    val s = LocalStrings.current
    val all = listOf(
        NavModuleItem(AppModule.DASHBOARD, AppRoutes.DASHBOARD, s.navDashboard, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        NavModuleItem(AppModule.CLIENTS, AppRoutes.CLIENTS, s.navClients, Icons.Outlined.People, Icons.Filled.People),
        NavModuleItem(AppModule.PROJECTS, AppRoutes.PROJECTS, s.navProjects, Icons.Outlined.WorkOutline, Icons.Filled.Work),
        NavModuleItem(AppModule.TASKS, AppRoutes.TASKS, s.navTasks, Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle),
        NavModuleItem(AppModule.SERVICES, AppRoutes.SERVICES, s.navServices, Icons.Outlined.DesignServices, Icons.Filled.DesignServices),
        NavModuleItem(AppModule.SALES, AppRoutes.SALES, s.navSales, Icons.Outlined.TrendingUp, Icons.Filled.TrendingUp),
        NavModuleItem(AppModule.FINANCE, AppRoutes.FINANCE, s.navFinance, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
        NavModuleItem(AppModule.INVOICES, AppRoutes.INVOICES, s.navInvoices, Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
        NavModuleItem(AppModule.MARKETING, AppRoutes.MARKETING, s.navMarketing, Icons.Outlined.Campaign, Icons.Filled.Campaign),
        NavModuleItem(AppModule.DOCUMENTS, AppRoutes.DOCUMENTS, s.navDocuments, Icons.Outlined.FolderOpen, Icons.Filled.Folder),
        NavModuleItem(AppModule.KNOWLEDGE, AppRoutes.KNOWLEDGE, s.navKnowledge, Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
        NavModuleItem(AppModule.REPORTS, AppRoutes.REPORTS, s.navReports, Icons.Outlined.Assessment, Icons.Filled.Assessment),
        NavModuleItem(AppModule.TEAM, AppRoutes.TEAM, s.navTeam, Icons.Outlined.Groups, Icons.Filled.Groups),
        NavModuleItem(AppModule.BACKUP, AppRoutes.BACKUP, s.navBackup, Icons.Outlined.Backup, Icons.Filled.Backup),
        NavModuleItem(AppModule.SETTINGS, AppRoutes.SETTINGS, s.navSettings, Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    return all.filter { state.canAccess(it.module) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasluneShell(
    state: BasluneUiState,
    currentRoute: String,
    pageTitle: String,
    isDetailScreen: Boolean,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onQuickAction: (QuickActionType) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val strings = LocalStrings.current
    val items = rememberModuleItems(state)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showQuickActions by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        BackHandler(enabled = !isWide && drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }

        val drawerContent: @Composable () -> Unit = {
            ModalDrawerSheet(
                drawerContainerColor = DeepEspresso,
                drawerContentColor = Ivory,
                modifier = Modifier.width(288.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // BASLUNE Studio Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RichBrown),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "B",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ChampagneGold
                                )
                            }
                            Column {
                                Text(
                                    text = state.settings.businessName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChampagneGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.appName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ivory.copy(alpha = 0.72f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StatusBadge(
                            text = "${strings.activeRole}: ${strings.label(state.settings.activeRole)}",
                            tone = BadgeTone.GOLD
                        )
                    }
                    HorizontalDivider(color = RichBrown)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = item.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigate(item.route)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.activeIcon else item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = RichBrown,
                                    selectedIconColor = ChampagneGold,
                                    selectedTextColor = ChampagneGold,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = Ivory.copy(alpha = 0.78f),
                                    unselectedTextColor = Ivory.copy(alpha = 0.88f)
                                ),
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .testTag("drawer_nav_${item.module.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        val mainScaffold: @Composable () -> Unit = {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            if (isDetailScreen) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("nav_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = strings.back
                                    )
                                }
                            } else if (!isWide) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("open_drawer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = strings.allModules
                                    )
                                }
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = pageTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${state.settings.businessName} • ${strings.label(state.settings.activeRole)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { onNavigate(AppRoutes.SEARCH) },
                                modifier = Modifier.testTag("global_search_button")
                            ) {
                                Icon(Icons.Outlined.Search, contentDescription = strings.search)
                            }
                            IconButton(
                                onClick = { onNavigate(AppRoutes.NOTIFICATIONS) },
                                modifier = Modifier.testTag("notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (state.unreadNotificationsCount > 0) {
                                            Badge {
                                                Text(
                                                    text = state.unreadNotificationsCount.coerceAtMost(99).toString()
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Outlined.Notifications, contentDescription = strings.notifications)
                                }
                            }
                            IconButton(
                                onClick = { showQuickActions = true },
                                modifier = Modifier.testTag("quick_actions_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DeepEspresso),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = strings.quickActions,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isWide && !isDetailScreen) {
                        val bottomCandidates = listOf(
                            AppModule.DASHBOARD,
                            AppModule.CLIENTS,
                            AppModule.PROJECTS,
                            AppModule.TASKS,
                            AppModule.FINANCE
                        ).mapNotNull { m -> items.firstOrNull { it.module == m } }

                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            bottomCandidates.forEach { navItem ->
                                val selected = currentRoute == navItem.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onNavigate(navItem.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) navItem.activeIcon else navItem.icon,
                                            contentDescription = navItem.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = navItem.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ChampagneGold,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = DeepEspresso,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("bottom_nav_${navItem.module.name.lowercase()}")
                                )
                            }
                        }
                    }
                },
                content = content
            )
        }

        if (isWide) {
            PermanentNavigationDrawer(
                drawerContent = drawerContent,
                content = mainScaffold
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = drawerContent,
                content = mainScaffold
            )
        }
    }

    if (showQuickActions) {
        QuickActionsSheet(
            state = state,
            onDismiss = { showQuickActions = false },
            onSelect = { action ->
                showQuickActions = false
                onQuickAction(action)
            }
        )
    }
}

enum class QuickActionType {
    ADD_CLIENT,
    ADD_PROJECT,
    ADD_TASK,
    RECORD_INCOME,
    RECORD_EXPENSE,
    CREATE_INVOICE,
    ADD_OPPORTUNITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionsSheet(
    state: BasluneUiState,
    onDismiss: () -> Unit,
    onSelect: (QuickActionType) -> Unit
) {
    val strings = LocalStrings.current
    val actions = listOf(
        Triple(QuickActionType.ADD_CLIENT, strings.qaAddClient, Icons.Outlined.PersonAdd) to AppModule.CLIENTS,
        Triple(QuickActionType.ADD_PROJECT, strings.qaAddProject, Icons.Outlined.CreateNewFolder) to AppModule.PROJECTS,
        Triple(QuickActionType.ADD_TASK, strings.qaAddTask, Icons.Outlined.AddTask) to AppModule.TASKS,
        Triple(QuickActionType.RECORD_INCOME, strings.qaRecordIncome, Icons.Outlined.ArrowDownward) to AppModule.FINANCE,
        Triple(QuickActionType.RECORD_EXPENSE, strings.qaRecordExpense, Icons.Outlined.ArrowUpward) to AppModule.FINANCE,
        Triple(QuickActionType.CREATE_INVOICE, strings.qaCreateInvoice, Icons.Outlined.ReceiptLong) to AppModule.INVOICES,
        Triple(QuickActionType.ADD_OPPORTUNITY, strings.qaAddOpportunity, Icons.Outlined.TrendingUp) to AppModule.SALES
    ).filter { (_, module) -> state.canAccess(module) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.quickActions,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            actions.forEach { (triple, _) ->
                val (type, label, icon) = triple
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onSelect(type) }
                        .testTag("quick_action_${type.name.lowercase()}"),
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(icon, contentDescription = label, tint = RichBrown)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
