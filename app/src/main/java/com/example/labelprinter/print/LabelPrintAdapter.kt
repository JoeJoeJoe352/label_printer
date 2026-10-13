package com.example.labelprinter.print

import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.example.labelprinter.model.LabelData
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Egyoldalas PDF-et készít a címkéből a nyomtató által kiválasztott
 * papírméretre. A szöveg vektorosan kerül a PDF-be, így bármilyen
 * felbontású nyomtatón éles marad.
 */
class LabelPrintAdapter(private val label: LabelData) : PrintDocumentAdapter() {

    private var attributes: PrintAttributes? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        attributes = newAttributes
        val info = PrintDocumentInfo.Builder(DOCUMENT_NAME)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()
        callback.onLayoutFinished(info, newAttributes != oldAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        val mediaSize = attributes?.mediaSize ?: LabelSpec.mediaSize
        // PDF egység: pont (1/72 hüvelyk); a méret ezred hüvelykben (mils) jön.
        val pageWidth = (mediaSize.widthMils / 1000f * POINTS_PER_INCH).roundToInt()
        val pageHeight = (mediaSize.heightMils / 1000f * POINTS_PER_INCH).roundToInt()

        val document = PdfDocument()
        try {
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            )
            LabelRenderer.draw(
                page.canvas,
                RectF(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat()),
                label,
            )
            document.finishPage(page)

            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
                return
            }
            FileOutputStream(destination.fileDescriptor).use { document.writeTo(it) }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback.onWriteFailed(e.message)
        } finally {
            document.close()
        }
    }

    private companion object {
        const val DOCUMENT_NAME = "cimke.pdf"
        const val POINTS_PER_INCH = 72f
    }
}
