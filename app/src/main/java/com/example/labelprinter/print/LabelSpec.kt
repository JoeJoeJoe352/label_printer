package com.example.labelprinter.print

import android.print.PrintAttributes

/**
 * A címke fizikai mérete. Alapértelmezés: 62 × 29 mm, ami a Brother QL
 * nyomtatók gyakori DK-11209 címkéje
 */
object LabelSpec {
    const val WIDTH_MM = 62f
    const val HEIGHT_MM = 29f

    /** Szélesség / magasság arány, ezt használja az előnézet is. */
    const val ASPECT_RATIO = WIDTH_MM / HEIGHT_MM

    private const val MILS_PER_MM = 1000f / 25.4f

    /** Egyedi papírméret a rendszer nyomtatási párbeszédablakához. */
    val mediaSize: PrintAttributes.MediaSize = PrintAttributes.MediaSize(
        "label_62x29",
        "Címke 62 × 29 mm",
        (WIDTH_MM * MILS_PER_MM).toInt(),
        (HEIGHT_MM * MILS_PER_MM).toInt(),
    )
}
