package com.example.labelprinter.print

import com.example.labelprinter.model.LabelData

/**
 * Nyomtatási absztrakció, hogy később más nyomtatót is lehessen használni.
 */
fun interface LabelPrinter {
    fun print(label: LabelData): PrintResult
}

sealed interface PrintResult {
    /** A nyomtatási feladat elindult (a rendszer párbeszédablaka megnyílt). */
    data object Started : PrintResult

    /** A nyomtatás nem indítható. */
    data class Failed(val reason: Reason, val details: String? = null) : PrintResult

    enum class Reason { PRINTING_UNAVAILABLE, UNEXPECTED_ERROR }
}
