package com.example

import androidx.compose.ui.unit.LayoutDirection
import com.example.core.localization.AppLanguage
import com.example.core.security.BusinessPermission
import com.example.core.security.InputValidator
import com.example.core.security.RolePermissions
import com.example.data.local.*
import com.example.data.repository.BackupManager
import com.example.data.repository.DatabaseSnapshot
import com.example.domain.logic.AnalyticsEngine
import com.example.domain.logic.FinancialCalculator
import com.example.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `languages map to expected RTL and LTR directions with Sorani default`() {
        assertEquals(AppLanguage.KURDISH_SORANI, AppLanguage.fromCode(null))
        assertEquals(LayoutDirection.Rtl, AppLanguage.KURDISH_SORANI.layoutDirection)
        assertEquals(LayoutDirection.Rtl, AppLanguage.ARABIC.layoutDirection)
        assertEquals(LayoutDirection.Ltr, AppLanguage.ENGLISH.layoutDirection)
    }

    @Test
    fun `financial calculator separates IQD and USD strictly and converts only with manual rate`() {
        val now = 1_700_000_000_000L
        val transactions = listOf(
            TransactionEntity(
                id = 1, type = TransactionType.INCOME, amount = 1_500_000.0,
                currency = CurrencyCode.IQD, category = TransactionCategory.INCOME_WEBSITE_DEVELOPMENT,
                date = now, status = TransactionStatus.COMPLETED
            ),
            TransactionEntity(
                id = 2, type = TransactionType.INCOME, amount = 1000.0,
                currency = CurrencyCode.USD, category = TransactionCategory.INCOME_APP_DEVELOPMENT,
                date = now, status = TransactionStatus.COMPLETED
            ),
            TransactionEntity(
                id = 3, type = TransactionType.EXPENSE, amount = 300_000.0,
                currency = CurrencyCode.IQD, category = TransactionCategory.EXPENSE_INTERNET,
                date = now, status = TransactionStatus.COMPLETED
            ),
            TransactionEntity(
                id = 4, type = TransactionType.EXPENSE, amount = 200.0,
                currency = CurrencyCode.USD, category = TransactionCategory.EXPENSE_SOFTWARE,
                date = now, status = TransactionStatus.COMPLETED
            )
        )
        val invoices = listOf(
            InvoiceEntity(
                id = 1, invoiceNumber = "INV-1", clientNameSnapshot = "Test",
                currency = CurrencyCode.USD, subtotal = 1500.0, total = 1500.0,
                paidAmount = 1000.0, status = InvoiceStatus.PARTIALLY_PAID,
                issueDate = now, dueDate = now + 86_400_000L
            )
        )

        val summary = FinancialCalculator.calculateSummary(
            transactions = transactions,
            invoices = invoices,
            preset = DateRangePreset.ALL_TIME,
            usdToIqdRate = 1500.0,
            nowMillis = now
        )

        assertEquals(1_500_000.0, summary.income.iqd, 0.01)
        assertEquals(1000.0, summary.income.usd, 0.01)
        assertEquals(300_000.0, summary.expenses.iqd, 0.01)
        assertEquals(200.0, summary.expenses.usd, 0.01)
        assertEquals(1_200_000.0, summary.netProfit.iqd, 0.01)
        assertEquals(800.0, summary.netProfit.usd, 0.01)
        assertEquals(500.0, summary.outstandingReceivables.usd, 0.01)
        // Manual rate combined check: 1,200,000 + (800 * 1500) = 2,400,000 IQD
        assertEquals(2_400_000.0, summary.netProfit.combinedInIqd(1500.0), 0.01)
    }

    @Test
    fun `invoice totals and status resolution handle discounts and partial payments`() {
        val (sub, disc, total) = FinancialCalculator.computeInvoiceTotals(
            itemTotals = listOf(500.0, 300.0),
            discount = 100.0,
            tax = 50.0
        )
        assertEquals(800.0, sub, 0.01)
        assertEquals(100.0, disc, 0.01)
        assertEquals(750.0, total, 0.01)

        val now = 1_700_000_000_000L
        assertEquals(
            InvoiceStatus.PARTIALLY_PAID,
            FinancialCalculator.resolveInvoiceStatus(InvoiceStatus.SENT, 750.0, 250.0, now + 1000, now)
        )
        assertEquals(
            InvoiceStatus.PAID,
            FinancialCalculator.resolveInvoiceStatus(InvoiceStatus.SENT, 750.0, 750.0, now + 1000, now)
        )
        assertEquals(
            InvoiceStatus.OVERDUE,
            FinancialCalculator.resolveInvoiceStatus(InvoiceStatus.SENT, 750.0, 0.0, now - 1000, now)
        )
    }

    @Test
    fun `rbac enforces permissions in business logic`() {
        assertTrue(RolePermissions.hasPermission(UserRole.OWNER, BusinessPermission.BACKUP_RESTORE))
        assertFalse(RolePermissions.hasPermission(UserRole.STAFF, BusinessPermission.VIEW_FINANCE))
        assertFalse(RolePermissions.hasPermission(UserRole.ADMINISTRATOR, BusinessPermission.EDIT_FINANCE))
        assertTrue(RolePermissions.hasPermission(UserRole.FINANCE, BusinessPermission.RECORD_PAYMENTS))
        assertFalse(RolePermissions.hasPermission(UserRole.FINANCE, BusinessPermission.EDIT_PROJECTS))
    }

    @Test
    fun `backup manager exports escaped CSV cleanly`() {
        val csv = BackupManager.exportCsv(
            headers = listOf("Client", "Notes"),
            rows = listOf(listOf("Zanko, Inc", "Line \"A\""))
        )
        assertTrue(csv.contains("\"Zanko, Inc\""))
        assertTrue(csv.contains("\"Line \"\"A\"\"\""))
    }

    @Test
    fun `input validator checks URLs and amounts`() {
        assertTrue(InputValidator.isValidWebUrl("https://baslune-creative-studio.ai.studio/"))
        assertTrue(InputValidator.isGoogleDriveUrl("https://drive.google.com/file/d/123/view"))
        assertFalse(InputValidator.isValidWebUrl("not-a-url"))
        assertTrue(InputValidator.isPositiveAmount("250.5"))
        assertFalse(InputValidator.isPositiveAmount("-10"))
    }

    @Test
    fun `analytics calculates conversion rate accurately`() {
        val opps = listOf(
            OpportunityEntity(title = "A", potentialClientName = "X", stage = OpportunityStage.WON),
            OpportunityEntity(title = "B", potentialClientName = "Y", stage = OpportunityStage.LOST),
            OpportunityEntity(title = "C", potentialClientName = "Z", stage = OpportunityStage.NEGOTIATION)
        )
        assertEquals(50.0, AnalyticsEngine.pipelineConversionRate(opps), 0.01)
    }

    @Test
    fun `formatters parseDateOrNull validates strict yyyy-MM-dd format`() {
        assertNotNull(com.example.core.localization.Formatters.parseDateOrNull("2026-11-19"))
        assertNull(com.example.core.localization.Formatters.parseDateOrNull(""))
        assertNull(com.example.core.localization.Formatters.parseDateOrNull("2026-13-40"))
        assertNull(com.example.core.localization.Formatters.parseDateOrNull("2026/11/19"))
        assertNull(com.example.core.localization.Formatters.parseDateOrNull("invalid"))
    }
}
