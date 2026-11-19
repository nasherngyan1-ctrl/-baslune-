package com.example.core.print

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.core.localization.AppLanguage
import com.example.core.localization.Formatters
import com.example.data.local.BusinessSettings
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import java.io.FileOutputStream

object InvoicePdfPrinter {
    fun printInvoice(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        settings: BusinessSettings,
        language: AppLanguage
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "BASLUNE_Invoice_${invoice.invoiceNumber}"

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdf = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas = page.canvas

                val bgPaint = Paint().apply { color = Color.parseColor("#FAF8F3") }
                canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

                val headerPaint = Paint().apply { color = Color.parseColor("#211B17") }
                canvas.drawRect(0f, 0f, 595f, 120f, headerPaint)

                val goldPaint = Paint().apply {
                    color = Color.parseColor("#D6C7A3")
                    textSize = 22f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val whiteSmall = Paint().apply {
                    color = Color.parseColor("#FAF8F3")
                    textSize = 11f
                }
                val darkTitle = Paint().apply {
                    color = Color.parseColor("#29231F")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val darkText = Paint().apply {
                    color = Color.parseColor("#29231F")
                    textSize = 11f
                }
                val mutedText = Paint().apply {
                    color = Color.parseColor("#777067")
                    textSize = 10f
                }
                val linePaint = Paint().apply {
                    color = Color.parseColor("#E8DFD3")
                    strokeWidth = 1f
                }

                // Header
                canvas.drawText(settings.businessName, 40f, 48f, goldPaint)
                canvas.drawText(settings.tagline, 40f, 68f, whiteSmall)
                canvas.drawText("${settings.location} • ${settings.contactEmail}", 40f, 88f, whiteSmall)

                val invTitlePaint = Paint(goldPaint).apply {
                    textAlign = Paint.Align.RIGHT
                    textSize = 18f
                }
                canvas.drawText("INVOICE ${invoice.invoiceNumber}", 555f, 52f, invTitlePaint)

                // Client & Dates
                var y = 160f
                canvas.drawText("Bill To:", 40f, y, mutedText)
                canvas.drawText(invoice.clientNameSnapshot, 40f, y + 18f, darkTitle)

                val rightAlign = Paint(darkText).apply { textAlign = Paint.Align.RIGHT }
                canvas.drawText("Issue Date: ${Formatters.formatDate(invoice.issueDate)}", 555f, y, rightAlign)
                canvas.drawText("Due Date: ${Formatters.formatDate(invoice.dueDate)}", 555f, y + 18f, rightAlign)
                canvas.drawText("Status: ${invoice.status.name}", 555f, y + 36f, rightAlign)

                y = 235f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 22f
                canvas.drawText("Item", 40f, y, darkTitle)
                canvas.drawText("Qty", 340f, y, darkTitle)
                canvas.drawText("Unit Price", 400f, y, darkTitle)
                canvas.drawText("Total", 555f, y, Paint(darkTitle).apply { textAlign = Paint.Align.RIGHT })
                y += 10f
                canvas.drawLine(40f, y, 555f, y, linePaint)

                y += 22f
                items.forEach { item ->
                    if (y < 700f) {
                        canvas.drawText(item.title.take(42), 40f, y, darkText)
                        canvas.drawText("${item.quantity}", 345f, y, darkText)
                        canvas.drawText(Formatters.formatMoney(item.unitPrice, invoice.currency, language), 400f, y, darkText)
                        canvas.drawText(Formatters.formatMoney(item.lineTotal, invoice.currency, language), 555f, y, rightAlign)
                        y += 22f
                    }
                }

                y += 12f
                canvas.drawLine(320f, y, 555f, y, linePaint)
                y += 24f
                canvas.drawText("Subtotal:", 340f, y, mutedText)
                canvas.drawText(Formatters.formatMoney(invoice.subtotal, invoice.currency, language), 555f, y, rightAlign)
                if (invoice.discount > 0.0) {
                    y += 18f
                    canvas.drawText("Discount:", 340f, y, mutedText)
                    canvas.drawText("-${Formatters.formatMoney(invoice.discount, invoice.currency, language)}", 555f, y, rightAlign)
                }
                if (invoice.tax > 0.0) {
                    y += 18f
                    canvas.drawText("Tax:", 340f, y, mutedText)
                    canvas.drawText(Formatters.formatMoney(invoice.tax, invoice.currency, language), 555f, y, rightAlign)
                }
                y += 22f
                canvas.drawText("Total:", 340f, y, darkTitle)
                canvas.drawText(Formatters.formatMoney(invoice.total, invoice.currency, language), 555f, y, Paint(darkTitle).apply { textAlign = Paint.Align.RIGHT })
                y += 20f
                canvas.drawText("Paid:", 340f, y, mutedText)
                canvas.drawText(Formatters.formatMoney(invoice.paidAmount, invoice.currency, language), 555f, y, rightAlign)
                y += 20f
                val balance = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)
                canvas.drawText("Balance Due:", 340f, y, darkTitle)
                canvas.drawText(Formatters.formatMoney(balance, invoice.currency, language), 555f, y, Paint(darkTitle).apply { textAlign = Paint.Align.RIGHT })

                if (invoice.notes.isNotBlank()) {
                    y += 42f
                    canvas.drawText("Notes: ${invoice.notes.take(120)}", 40f, y, mutedText)
                }

                pdf.finishPage(page)
                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        pdf.writeTo(out)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdf.close()
                }
            }
        }

        printManager.print(jobName, adapter, null)
    }
}
