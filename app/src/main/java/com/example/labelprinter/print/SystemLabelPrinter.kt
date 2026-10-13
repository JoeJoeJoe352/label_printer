package com.example.labelprinter.print

import android.app.Activity
import android.print.PrintAttributes
import android.print.PrintManager
import com.example.labelprinter.model.LabelData

/**
 * Az Android beépített nyomtatási rendszerét ([PrintManager]) használó implementáció.
 *
 * Activity paraméter kell hozzá, mert a [PrintManager.print] párbeszédablakot nyit.
 */
class SystemLabelPrinter(private val activity: Activity) : LabelPrinter {

    override fun print(label: LabelData): PrintResult {
        val printManager = activity.getSystemService(PrintManager::class.java)
            ?: return PrintResult.Failed(PrintResult.Reason.PRINTING_UNAVAILABLE)

        val attributes = PrintAttributes.Builder()
            .setMediaSize(LabelSpec.mediaSize)
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        return try {
            printManager.print(JOB_NAME, LabelPrintAdapter(label), attributes)
            PrintResult.Started
        } catch (e: RuntimeException) {
            PrintResult.Failed(PrintResult.Reason.UNEXPECTED_ERROR, e.message)
        }
    }

    private companion object {
        const val JOB_NAME = "Címke"
    }
}
