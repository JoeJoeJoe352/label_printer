package com.example.labelprinter.print

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.labelprinter.model.LabelData
import kotlin.math.min

/**
 * A címke kirajzolása. Ugyanezt a kódot használja a képernyős előnézet és a
 * nyomtatandó PDF is, így ami a képernyőn látszik, pontosan az kerül a címkére.
 */
object LabelRenderer {

    private const val LINE_SPACING = 1.25f
    private const val PADDING_RATIO = 0.08f

    /**
     * A címkét a [bounds] téglalapba rajzolja: a címke arányait megtartva
     * középre igazítja, a betűméretet pedig úgy választja, hogy a leghosszabb
     * sor is kiférjen.
     */
    fun draw(canvas: Canvas, bounds: RectF, label: LabelData) {
        val area = fitAspect(bounds)
        val padding = min(area.width(), area.height()) * PADDING_RATIO
        val availableWidth = area.width() - 2 * padding
        val availableHeight = area.height() - 2 * padding

        val paints = label.lines.indices.map { index ->
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.BLACK
                // Az első sor (név) félkövér, a cím többi része normál.
                typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
        }

        // 1) Betűméret a magasság alapján.
        var textSize = availableHeight / (label.lines.size * LINE_SPACING)
        paints.forEach { it.textSize = textSize }

        // 2) Ha a leghosszabb sor nem fér ki, arányosan kicsinyítünk.
        val widest = label.lines.zip(paints).maxOf { (text, paint) -> paint.measureText(text) }
        if (widest > availableWidth) {
            textSize *= availableWidth / widest
            paints.forEach { it.textSize = textSize }
        }

        // 3) Sorok kirajzolása függőlegesen középre igazítva.
        val lineHeight = textSize * LINE_SPACING
        val blockHeight = lineHeight * label.lines.size
        val metrics = paints.first().fontMetrics
        val glyphHeight = metrics.descent - metrics.ascent
        var baseline = area.top + padding + (availableHeight - blockHeight) / 2 +
            (lineHeight - glyphHeight) / 2 - metrics.ascent

        label.lines.zip(paints).forEach { (text, paint) ->
            canvas.drawText(text, area.left + padding, baseline, paint)
            baseline += lineHeight
        }
    }

    /** A [bounds]-ba illeszthető legnagyobb középre igazított téglalap. */
    private fun fitAspect(bounds: RectF): RectF {
        val aspect = LabelSpec.ASPECT_RATIO
        val boundsAspect = bounds.width() / bounds.height()
        return if (boundsAspect > aspect) {
            val width = bounds.height() * aspect
            val left = bounds.left + (bounds.width() - width) / 2
            RectF(left, bounds.top, left + width, bounds.bottom)
        } else {
            val height = bounds.width() / aspect
            val top = bounds.top + (bounds.height() - height) / 2
            RectF(bounds.left, top, bounds.right, top + height)
        }
    }
}
