package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.repository.LocalBasluneRepository
import com.example.data.seed.StarterDataSeeder
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: BasluneDatabase
    private lateinit var settingsStore: SettingsDataStore
    private lateinit var repo: LocalBasluneRepository

    @Before
    fun setup() = kotlinx.coroutines.test.runTest {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BasluneDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsStore = SettingsDataStore(context)
        settingsStore.setStarterSeeded(false)
        settingsStore.setSampleDataActive(false)
        settingsStore.setActiveRole(UserRole.OWNER)
        repo = LocalBasluneRepository(
            db = db,
            dao = db.basluneDao(),
            settingsStore = settingsStore,
            seeder = StarterDataSeeder()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches BASLUNE OS`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("BASLUNE OS", appName)
    }

    @Test
    fun `starter seed adds 4 non-revenue showcase projects and zero fake client revenue`() = runTest {
        repo.ensureInitialSeed()
        val projects = repo.projects.first()
        val clients = repo.clients.first()
        val tx = repo.transactions.first()

        assertEquals(4, projects.size)
        assertTrue(projects.all { it.isStarter && it.agreedPrice == 0.0 })
        assertTrue(clients.isEmpty())
        assertTrue(tx.isEmpty())
    }

    @Test
    fun `end to end client project task invoice payment and profitability relationships`() = runTest {
        repo.ensureInitialSeed()

        // 1. Create client & link project
        val clientId = repo.upsertClient(
            ClientEntity(fullName = "Kurdistan Tech Co", city = "Erbil", status = ClientStatus.ACTIVE)
        )
        val projectId = repo.upsertProject(
            ProjectEntity(
                name = "Kurdistan Tech Web",
                clientId = clientId,
                agreedPrice = 2000.0,
                currency = CurrencyCode.USD,
                status = ProjectStatus.IN_PROGRESS
            )
        )

        // 2. Add 2 tasks and complete 1 -> project progress becomes 50%
        val t1Id = repo.upsertTask(TaskEntity(title = "Design UI", projectId = projectId, clientId = clientId))
        repo.upsertTask(TaskEntity(title = "Deploy App", projectId = projectId, clientId = clientId))
        val t1 = repo.tasks.first().first { it.id == t1Id }
        repo.toggleTaskCompleted(t1, true)

        val updatedProject = repo.projects.first().first { it.id == projectId }
        assertEquals(50, updatedProject.progressPercent)

        // 3. Create invoice & record partial payment -> updates balance & creates single cash transaction
        val invId = repo.saveInvoiceWithItems(
            InvoiceEntity(
                invoiceNumber = "INV-100",
                clientId = clientId,
                clientNameSnapshot = "Kurdistan Tech Co",
                projectId = projectId,
                currency = CurrencyCode.USD,
                status = InvoiceStatus.SENT
            ),
            listOf(InvoiceItemEntity(invoiceId = 0, title = "Web Build", quantity = 1, unitPrice = 2000.0, lineTotal = 2000.0))
        )
        val savedInv = repo.invoices.first().first { it.id == invId }
        repo.recordInvoicePayment(savedInv, 800.0, PaymentMethod.FIB, "First installment")

        val invAfterPay = repo.invoices.first().first { it.id == invId }
        assertEquals(800.0, invAfterPay.paidAmount, 0.01)
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invAfterPay.status)

        // 4. Add project expense & check profitability
        repo.upsertTransaction(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 200.0,
                currency = CurrencyCode.USD,
                category = TransactionCategory.EXPENSE_SOFTWARE,
                projectId = projectId
            )
        )

        val allTx = repo.transactions.first()
        val prof = FinancialCalculator.calculateProjectProfitability(projectId, allTx)
        assertEquals(800.0, prof.incomeUsd, 0.01)
        assertEquals(200.0, prof.expenseUsd, 0.01)
        assertEquals(600.0, prof.profitUsd, 0.01)
    }

    @Test
    fun `rbac blocks non-finance staff role from recording transactions`() = runTest {
        repo.setActiveRole(UserRole.STAFF, "Staff User")
        var blocked = false
        try {
            repo.upsertTransaction(
                TransactionEntity(
                    type = TransactionType.INCOME,
                    amount = 100.0,
                    currency = CurrencyCode.USD,
                    category = TransactionCategory.INCOME_OTHER
                )
            )
        } catch (_: SecurityException) {
            blocked = true
        }
        assertTrue(blocked)
    }

    @Test
    fun `backup manager validates schema version and round-trips data`() = runTest {
        repo.ensureInitialSeed()
        repo.upsertClient(ClientEntity(clientCode = "CL-1", fullName = "Erbil Corp"))
        val snapshot = repo.getDatabaseSnapshot()
        val json = com.example.data.repository.BackupManager.exportToJson(snapshot)
        val parsed = com.example.data.repository.BackupManager.validateAndParse(json)
        assertTrue(parsed is com.example.data.repository.BackupManager.ValidationResult.Valid)
        val valid = parsed as com.example.data.repository.BackupManager.ValidationResult.Valid
        assertEquals(1, valid.snapshot.clients.size)
        assertEquals("Erbil Corp", valid.snapshot.clients.first().fullName)

        val badVersion = json.replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        assertTrue(com.example.data.repository.BackupManager.validateAndParse(badVersion) is com.example.data.repository.BackupManager.ValidationResult.Invalid)
    }

    @Test
    fun `application package id is com baslune os`() {
        assertEquals("com.baslune.os", context.packageName)
    }

    @Test
    fun `kurdish arabic and english languages configure expected rtl ltr directions and strings`() {
        val ckb = com.example.core.localization.AppLanguage.KURDISH_SORANI
        val ar = com.example.core.localization.AppLanguage.ARABIC
        val en = com.example.core.localization.AppLanguage.ENGLISH

        assertTrue(ckb.isRtl)
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, ckb.layoutDirection)
        assertTrue(ar.isRtl)
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, ar.layoutDirection)
        assertFalse(en.isRtl)
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Ltr, en.layoutDirection)

        assertTrue(com.example.core.localization.stringsFor(ckb).navProjects.isNotBlank())
        assertTrue(com.example.core.localization.stringsFor(ar).navProjects.isNotBlank())
        assertEquals("Projects", com.example.core.localization.stringsFor(en).navProjects)
    }
}
