package com.example

import android.content.Context
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.example.core.localization.AppLanguage
import com.example.core.localization.Formatters
import com.example.data.local.*
import com.example.data.repository.LocalBasluneRepository
import com.example.data.seed.StarterDataSeeder
import com.example.domain.model.*
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.projects.ProjectDetailScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.theme.BasluneTheme
import com.example.ui.viewmodel.BasluneMainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ProjectsModuleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: BasluneDatabase
    private lateinit var queryExecutor: ExecutorService
    private lateinit var settingsStore: SettingsDataStore
    private lateinit var repo: LocalBasluneRepository
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        queryExecutor = Executors.newSingleThreadExecutor()
        viewModelStore = ViewModelStore()
        db = Room.inMemoryDatabaseBuilder(context, BasluneDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(queryExecutor)
            .build()
        settingsStore = SettingsDataStore(context)
        settingsStore.setStarterSeeded(false)
        settingsStore.setSampleDataActive(false)
        settingsStore.setActiveRole(UserRole.OWNER)
        settingsStore.setLanguage(AppLanguage.ENGLISH)
        repo = LocalBasluneRepository(
            db = db,
            dao = db.basluneDao(),
            settingsStore = settingsStore,
            seeder = StarterDataSeeder()
        )
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        shadowOf(Looper.getMainLooper()).idle()
        queryExecutor.shutdown()
        queryExecutor.awaitTermination(2, TimeUnit.SECONDS)
        shadowOf(Looper.getMainLooper()).idle()
        db.close()
    }

    private fun createViewModel(): BasluneMainViewModel {
        return ViewModelProvider(
            viewModelStore,
            BasluneMainViewModel.Factory(repo)
        )[BasluneMainViewModel::class.java]
    }

    private fun waitForCondition(timeoutMillis: Long = 5000, condition: () -> Boolean) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    @Test
    fun `repository creates updates and deletes user projects cleanly`() = runTest {
        repo.ensureInitialSeed()
        val clientId = repo.upsertClient(ClientEntity(fullName = "Erbil Studio Client"))
        val start = Formatters.parseDateOrNull("2026-10-01")!!
        val deadline = Formatters.parseDateOrNull("2026-11-30")!!

        val projectId = repo.upsertProject(
            ProjectEntity(
                name = "Tigris E-Commerce",
                clientId = clientId,
                description = "Initial scope",
                status = ProjectStatus.PLANNING,
                priority = PriorityLevel.NORMAL,
                startDate = start,
                deadline = deadline,
                budget = 1500.0,
                agreedPrice = 2000.0,
                currency = CurrencyCode.USD,
                internalNotes = "Kickoff pending"
            )
        )

        val created = repo.projects.first().first { it.id == projectId }
        assertEquals("Tigris E-Commerce", created.name)
        assertEquals(1500.0, created.budget, 0.01)
        assertEquals("Kickoff pending", created.internalNotes)

        // Edit all key fields
        val newStart = Formatters.parseDateOrNull("2026-10-05")!!
        val newDeadline = Formatters.parseDateOrNull("2026-12-15")!!
        repo.upsertProject(
            created.copy(
                name = "Tigris E-Commerce v2",
                description = "Updated scope with mobile checkout",
                status = ProjectStatus.IN_PROGRESS,
                priority = PriorityLevel.URGENT,
                startDate = newStart,
                deadline = newDeadline,
                budget = 2200.0,
                agreedPrice = 2800.0,
                internalNotes = "Approved by client"
            )
        )

        val updated = repo.projects.first().first { it.id == projectId }
        assertEquals("Tigris E-Commerce v2", updated.name)
        assertEquals("Updated scope with mobile checkout", updated.description)
        assertEquals(ProjectStatus.IN_PROGRESS, updated.status)
        assertEquals(PriorityLevel.URGENT, updated.priority)
        assertEquals(newStart, updated.startDate)
        assertEquals(newDeadline, updated.deadline)
        assertEquals(2200.0, updated.budget, 0.01)
        assertEquals(2800.0, updated.agreedPrice, 0.01)
        assertEquals("Approved by client", updated.internalNotes)

        // Link milestone and task, then delete project
        repo.upsertMilestone(ProjectMilestoneEntity(projectId = projectId, title = "M1"))
        val taskId = repo.upsertTask(TaskEntity(title = "Task linked to project", projectId = projectId))

        repo.deleteProject(projectId)

        assertFalse(repo.projects.first().any { it.id == projectId })
        assertTrue(repo.milestones.first().none { it.projectId == projectId })
        val preservedTask = repo.tasks.first().first { it.id == taskId }
        assertNull(preservedTask.projectId)
    }

    @Test
    fun `rbac prevents unauthorized roles from deleting or editing projects`() = runTest {
        repo.ensureInitialSeed()
        val firstProject = repo.projects.first().first()

        // Staff can edit, cannot delete
        repo.setActiveRole(UserRole.STAFF, "Staff Member")
        repo.upsertProject(firstProject.copy(internalNotes = "Staff note"))
        var deleteBlocked = false
        try {
            repo.deleteProject(firstProject.id)
        } catch (_: SecurityException) {
            deleteBlocked = true
        }
        assertTrue(deleteBlocked)

        // Finance cannot edit or delete
        repo.setActiveRole(UserRole.FINANCE, "Finance Member")
        var editBlocked = false
        try {
            repo.upsertProject(firstProject.copy(name = "Should Fail"))
        } catch (_: SecurityException) {
            editBlocked = true
        }
        assertTrue(editBlocked)
    }

    @Test
    fun `ui creates edits cancels validates and deletes projects and updates dashboard`() {
        val viewModel = createViewModel()
        var showDashboard by mutableStateOf(false)

        composeRule.setContent {
            val liveState by viewModel.uiState.collectAsStateWithLifecycle()
            BasluneTheme(language = AppLanguage.ENGLISH) {
                if (liveState.isReady) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showDashboard) {
                            DashboardScreen(
                                state = liveState,
                                viewModel = viewModel,
                                onNavigate = {}
                            )
                        } else {
                            ProjectsScreen(
                                state = liveState,
                                viewModel = viewModel,
                                onOpenProject = {}
                            )
                        }
                    }
                }
            }
        }

        waitForCondition {
            composeRule.onAllNodesWithTag("add_project_fab").fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Create a new project via UI
        composeRule.onNodeWithTag("add_project_fab").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("project_name_input").performScrollTo().performTextInput("Citadel Brand Portal")
        composeRule.onNodeWithTag("project_budget_input").performScrollTo().performTextInput("1800")
        composeRule.onNodeWithTag("project_notes_input").performScrollTo().performTextInput("Initial luxury brief")
        composeRule.onNodeWithTag("save_project_button").performClick()

        waitForCondition {
            viewModel.uiState.value.projects.any { it.name == "Citadel Brand Portal" } &&
                composeRule.onAllNodesWithText("Citadel Brand Portal").fetchSemanticsNodes().isNotEmpty()
        }

        val createdProject = viewModel.uiState.value.projects.first { it.name == "Citadel Brand Portal" }

        // Verify Dashboard immediately reflects the newly created project
        showDashboard = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dashboard_screen").performScrollToNode(hasText("Citadel Brand Portal"))
        composeRule.onNodeWithText("Citadel Brand Portal").assertExists()

        // Switch back to Projects
        showDashboard = false
        composeRule.waitForIdle()

        // 2. Open Edit, modify name, then CANCEL -> changes must NOT persist
        composeRule.onNodeWithTag("edit_project_btn_${createdProject.id}").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("project_name_input").performScrollTo().performTextReplacement("Cancelled Name")
        composeRule.onNodeWithTag("cancel_project_button").performClick()
        composeRule.waitForIdle()

        assertTrue(composeRule.onAllNodesWithText("Cancelled Name").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithText("Citadel Brand Portal").fetchSemanticsNodes().isNotEmpty())

        // 3. Open Edit, test invalid date validation, then fix and SAVE
        composeRule.onNodeWithTag("edit_project_btn_${createdProject.id}").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("project_deadline_input").performScrollTo().performTextReplacement("not-a-date")
        composeRule.onNodeWithTag("save_project_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("project_form_error").performScrollTo().assertExists()

        // Fix deadline and update all key fields
        composeRule.onNodeWithTag("project_name_input").performScrollTo().performTextReplacement("Citadel Brand Portal Pro")
        composeRule.onNodeWithTag("project_start_date_input").performScrollTo().performTextReplacement("2026-11-01")
        composeRule.onNodeWithTag("project_deadline_input").performScrollTo().performTextReplacement("2026-12-20")
        composeRule.onNodeWithTag("project_budget_input").performScrollTo().performTextReplacement("2500")
        composeRule.onNodeWithTag("project_description_input").performScrollTo().performTextReplacement("Full bilingual portal")
        composeRule.onNodeWithTag("project_notes_input").performScrollTo().performTextReplacement("Revised scope saved")
        composeRule.onNodeWithTag("save_project_button").performClick()

        waitForCondition {
            viewModel.uiState.value.projects.any { it.id == createdProject.id && it.name == "Citadel Brand Portal Pro" } &&
                composeRule.onAllNodesWithText("Citadel Brand Portal Pro").fetchSemanticsNodes().isNotEmpty()
        }

        val savedProject = viewModel.uiState.value.projects.first { it.id == createdProject.id }
        assertEquals(2500.0, savedProject.budget, 0.01)
        assertEquals("Full bilingual portal", savedProject.description)
        assertEquals("Revised scope saved", savedProject.internalNotes)

        // Verify Dashboard immediately reflects updated project name
        showDashboard = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dashboard_screen").performScrollToNode(hasText("Citadel Brand Portal Pro"))
        composeRule.onNodeWithText("Citadel Brand Portal Pro").assertExists()

        showDashboard = false
        composeRule.waitForIdle()

        // 4. Delete with confirmation dialog: first dismiss, then confirm
        composeRule.onNodeWithTag("delete_project_btn_${createdProject.id}").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_destructive_button").assertExists()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()

        // Still exists after cancelling delete
        assertTrue(composeRule.onAllNodesWithText("Citadel Brand Portal Pro").fetchSemanticsNodes().isNotEmpty())

        // Now confirm delete
        composeRule.onNodeWithTag("delete_project_btn_${createdProject.id}").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_destructive_button").performClick()

        waitForCondition {
            viewModel.uiState.value.projects.none { it.id == createdProject.id } &&
                composeRule.onAllNodesWithText("Citadel Brand Portal Pro").fetchSemanticsNodes().isEmpty()
        }
        assertFalse(viewModel.uiState.value.projects.any { it.id == createdProject.id })

        // Verify Dashboard also no longer displays deleted project
        showDashboard = true
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText("Citadel Brand Portal Pro").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `project detail screen supports editing and deleting real projects with confirmation`() {
        val id = runBlocking {
            repo.upsertProject(
                ProjectEntity(
                    name = "Detail Screen Project",
                    description = "Initial detail desc",
                    budget = 900.0,
                    agreedPrice = 1100.0,
                    internalNotes = "Detail notes"
                )
            )
        }
        val viewModel = createViewModel()
        var backCalled = false

        composeRule.setContent {
            val liveState by viewModel.uiState.collectAsStateWithLifecycle()
            BasluneTheme(language = AppLanguage.ENGLISH) {
                if (liveState.isReady) {
                    ProjectDetailScreen(
                        projectId = id,
                        state = liveState,
                        viewModel = viewModel,
                        onBack = { backCalled = true }
                    )
                }
            }
        }

        waitForCondition {
            composeRule.onAllNodesWithTag("detail_edit_project_btn").fetchSemanticsNodes().isNotEmpty()
        }

        // Edit from detail screen
        composeRule.onNodeWithTag("detail_edit_project_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("project_name_input").performScrollTo().performTextReplacement("Detail Screen Updated")
        composeRule.onNodeWithTag("save_project_button").performClick()

        waitForCondition {
            viewModel.uiState.value.projects.any { it.id == id && it.name == "Detail Screen Updated" } &&
                composeRule.onAllNodesWithText("Detail Screen Updated").fetchSemanticsNodes().isNotEmpty()
        }

        // Delete from detail screen -> confirmation dialog -> confirm -> onBack called & removed
        composeRule.onNodeWithTag("detail_delete_project_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_destructive_button").performClick()

        waitForCondition {
            backCalled && viewModel.uiState.value.projects.none { it.id == id }
        }
        assertFalse(viewModel.uiState.value.projects.any { it.id == id })
    }
}
