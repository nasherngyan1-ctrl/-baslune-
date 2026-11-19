package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.localization.LocalStrings
import com.example.domain.model.AppModule
import com.example.domain.model.TransactionType
import com.example.ui.navigation.AppRoutes
import com.example.ui.navigation.BasluneShell
import com.example.ui.navigation.QuickActionType
import com.example.ui.screens.backup.BackupCenterScreen
import com.example.ui.screens.clients.ClientDetailScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.documents.DocumentsScreen
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.invoices.InvoiceDetailScreen
import com.example.ui.screens.invoices.InvoicesScreen
import com.example.ui.screens.knowledge.KnowledgeScreen
import com.example.ui.screens.marketing.MarketingScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.projects.ProjectDetailScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.SalesScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.services.ServicesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.team.TeamScreen
import com.example.ui.theme.BasluneTheme
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.Ivory
import com.example.ui.theme.RichBrown
import com.example.ui.viewmodel.BasluneMainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        val app = application as BasluneApp
        setContent {
            val viewModel: BasluneMainViewModel = viewModel(
                factory = BasluneMainViewModel.Factory(app.repository)
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(state.isReady, state.settings.darkTheme) {
                val useDarkBars = !state.isReady || state.settings.darkTheme
                if (useDarkBars) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    )
                }
            }

            BasluneTheme(
                language = state.settings.language,
                darkTheme = state.settings.darkTheme
            ) {
                if (!state.isReady) {
                    BasluneStartupSplash()
                } else {
                    BasluneOsRoot(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun BasluneStartupSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepEspresso)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(84.dp),
                shape = RoundedCornerShape(20.dp),
                color = RichBrown,
                border = BorderStroke(1.5.dp, ChampagneGold)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "B",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "BASLUNE OS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ChampagneGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "BASLUNE Studio • Your Vision. Our Digital Craft.",
                style = MaterialTheme.typography.bodySmall,
                color = Ivory.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = ChampagneGold,
                trackColor = RichBrown,
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
private fun BasluneOsRoot(
    state: com.example.ui.viewmodel.BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingQuickAction by remember { mutableStateOf<QuickActionType?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val isDetailScreen = currentRoute.startsWith("client_detail") ||
        currentRoute.startsWith("project_detail") ||
        currentRoute.startsWith("invoice_detail") ||
        currentRoute == AppRoutes.SEARCH ||
        currentRoute == AppRoutes.NOTIFICATIONS ||
        currentRoute == AppRoutes.BACKUP

    val pageTitle = when {
        currentRoute == AppRoutes.DASHBOARD -> strings.navDashboard
        currentRoute == AppRoutes.CLIENTS -> strings.navClients
        currentRoute.startsWith("client_detail") -> strings.clientDetails
        currentRoute == AppRoutes.PROJECTS -> strings.navProjects
        currentRoute.startsWith("project_detail") -> strings.projectDetails
        currentRoute == AppRoutes.TASKS -> strings.navTasks
        currentRoute == AppRoutes.SERVICES -> strings.navServices
        currentRoute == AppRoutes.SALES -> strings.navSales
        currentRoute == AppRoutes.FINANCE -> strings.navFinance
        currentRoute == AppRoutes.INVOICES -> strings.navInvoices
        currentRoute.startsWith("invoice_detail") -> strings.invoiceDetails
        currentRoute == AppRoutes.MARKETING -> strings.navMarketing
        currentRoute == AppRoutes.DOCUMENTS -> strings.navDocuments
        currentRoute == AppRoutes.KNOWLEDGE -> strings.navKnowledge
        currentRoute == AppRoutes.REPORTS -> strings.navReports
        currentRoute == AppRoutes.TEAM -> strings.navTeam
        currentRoute == AppRoutes.SETTINGS -> strings.navSettings
        currentRoute == AppRoutes.BACKUP -> strings.navBackup
        currentRoute == AppRoutes.SEARCH -> strings.search
        currentRoute == AppRoutes.NOTIFICATIONS -> strings.notifications
        else -> strings.appName
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(AppRoutes.DASHBOARD) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BasluneShell(
        state = state,
        currentRoute = currentRoute,
        pageTitle = pageTitle,
        isDetailScreen = isDetailScreen,
        snackbarHostState = snackbarHostState,
        onNavigate = { route -> navigateTopLevel(route) },
        onBack = { navController.popBackStack() },
        onQuickAction = { action ->
            pendingQuickAction = action
            when (action) {
                QuickActionType.ADD_CLIENT -> navigateTopLevel(AppRoutes.CLIENTS)
                QuickActionType.ADD_PROJECT -> navigateTopLevel(AppRoutes.PROJECTS)
                QuickActionType.ADD_TASK -> navigateTopLevel(AppRoutes.TASKS)
                QuickActionType.RECORD_INCOME, QuickActionType.RECORD_EXPENSE -> navigateTopLevel(AppRoutes.FINANCE)
                QuickActionType.CREATE_INVOICE -> navigateTopLevel(AppRoutes.INVOICES)
                QuickActionType.ADD_OPPORTUNITY -> navigateTopLevel(AppRoutes.SALES)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.DASHBOARD
            ) {
                composable(AppRoutes.DASHBOARD) {
                    DashboardScreen(state = state, viewModel = viewModel, onNavigate = ::navigateTopLevel)
                }
                composable(AppRoutes.CLIENTS) {
                    if (state.canAccess(AppModule.CLIENTS)) {
                        ClientsScreen(
                            state = state,
                            viewModel = viewModel,
                            openAddImmediately = pendingQuickAction == QuickActionType.ADD_CLIENT,
                            onConsumeQuickAction = { pendingQuickAction = null },
                            onOpenClient = { id -> navController.navigate(AppRoutes.clientDetail(id)) }
                        )
                    }
                }
                composable(
                    route = AppRoutes.CLIENT_DETAIL,
                    arguments = listOf(navArgument("clientId") { type = NavType.LongType })
                ) { entry ->
                    val id = entry.arguments?.getLong("clientId") ?: 0L
                    ClientDetailScreen(
                        clientId = id,
                        state = state,
                        viewModel = viewModel,
                        onNavigate = ::navigateTopLevel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.PROJECTS) {
                    if (state.canAccess(AppModule.PROJECTS)) {
                        ProjectsScreen(
                            state = state,
                            viewModel = viewModel,
                            openAddImmediately = pendingQuickAction == QuickActionType.ADD_PROJECT,
                            onConsumeQuickAction = { pendingQuickAction = null },
                            onOpenProject = { id -> navController.navigate(AppRoutes.projectDetail(id)) }
                        )
                    }
                }
                composable(
                    route = AppRoutes.PROJECT_DETAIL,
                    arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                ) { entry ->
                    val id = entry.arguments?.getLong("projectId") ?: 0L
                    ProjectDetailScreen(
                        projectId = id,
                        state = state,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.TASKS) {
                    if (state.canAccess(AppModule.TASKS)) {
                        TasksScreen(
                            state = state,
                            viewModel = viewModel,
                            openAddImmediately = pendingQuickAction == QuickActionType.ADD_TASK,
                            onConsumeQuickAction = { pendingQuickAction = null }
                        )
                    }
                }
                composable(AppRoutes.SERVICES) {
                    if (state.canAccess(AppModule.SERVICES)) {
                        ServicesScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.SALES) {
                    if (state.canAccess(AppModule.SALES)) {
                        SalesScreen(
                            state = state,
                            viewModel = viewModel,
                            openAddImmediately = pendingQuickAction == QuickActionType.ADD_OPPORTUNITY,
                            onConsumeQuickAction = { pendingQuickAction = null }
                        )
                    }
                }
                composable(AppRoutes.FINANCE) {
                    if (state.canAccess(AppModule.FINANCE)) {
                        val quickTx = when (pendingQuickAction) {
                            QuickActionType.RECORD_INCOME -> TransactionType.INCOME
                            QuickActionType.RECORD_EXPENSE -> TransactionType.EXPENSE
                            else -> null
                        }
                        FinanceScreen(
                            state = state,
                            viewModel = viewModel,
                            quickActionType = quickTx,
                            onConsumeQuickAction = { pendingQuickAction = null }
                        )
                    }
                }
                composable(AppRoutes.INVOICES) {
                    if (state.canAccess(AppModule.INVOICES)) {
                        InvoicesScreen(
                            state = state,
                            viewModel = viewModel,
                            openCreateImmediately = pendingQuickAction == QuickActionType.CREATE_INVOICE,
                            onConsumeQuickAction = { pendingQuickAction = null },
                            onOpenInvoice = { id -> navController.navigate(AppRoutes.invoiceDetail(id)) }
                        )
                    }
                }
                composable(
                    route = AppRoutes.INVOICE_DETAIL,
                    arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
                ) { entry ->
                    val id = entry.arguments?.getLong("invoiceId") ?: 0L
                    InvoiceDetailScreen(
                        invoiceId = id,
                        state = state,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.MARKETING) {
                    if (state.canAccess(AppModule.MARKETING)) {
                        MarketingScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.DOCUMENTS) {
                    if (state.canAccess(AppModule.DOCUMENTS)) {
                        DocumentsScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.KNOWLEDGE) {
                    if (state.canAccess(AppModule.KNOWLEDGE)) {
                        KnowledgeScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.REPORTS) {
                    if (state.canAccess(AppModule.REPORTS)) {
                        ReportsScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.TEAM) {
                    if (state.canAccess(AppModule.TEAM)) {
                        TeamScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.SETTINGS) {
                    if (state.canAccess(AppModule.SETTINGS)) {
                        SettingsScreen(state = state, viewModel = viewModel, onNavigate = ::navigateTopLevel)
                    }
                }
                composable(AppRoutes.BACKUP) {
                    if (state.canAccess(AppModule.BACKUP)) {
                        BackupCenterScreen(state = state, viewModel = viewModel)
                    }
                }
                composable(AppRoutes.SEARCH) {
                    GlobalSearchScreen(state = state, onNavigate = ::navigateTopLevel, onBack = { navController.popBackStack() })
                }
                composable(AppRoutes.NOTIFICATIONS) {
                    NotificationsScreen(state = state, viewModel = viewModel, onNavigate = ::navigateTopLevel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
