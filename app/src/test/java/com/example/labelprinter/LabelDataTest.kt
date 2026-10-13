package com.example.labelprinter

import com.example.labelprinter.model.LabelData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelDataTest {

    @Test
    fun `a címke három sorból áll, név - város - utca sorrendben`() {
        val label = LabelData("John Doe", "Hódmezővásárhely", "Andrássy út 12.")
        assertEquals(listOf("John Doe", "Hódmezővásárhely", "Andrássy út 12."), label.lines)
    }

    @Test
    fun `a sanitize legfeljebb 32 karaktert hagy meg`() {
        val long = "a".repeat(100)
        assertEquals(LabelData.MAX_LENGTH, LabelData.sanitize(long).length)
        assertEquals(32, LabelData.MAX_LENGTH)
    }

    @Test
    fun `a sanitize a rövid szöveget változatlanul hagyja, ékezetekkel együtt`() {
        assertEquals("Hódmezővásárhely", LabelData.sanitize("Hódmezővásárhely"))
    }

    @Test
    fun `a sanitize a beillesztett sortöréseket szóközre cseréli`() {
        assertEquals("Andrássy út 12.", LabelData.sanitize("Andrássy út\n12."))
    }

    @Test
    fun `a trimmed levágja a szélső szóközöket`() {
        val label = LabelData("  John Doe ", " Szeged", "Hajós u. 28.  ").trimmed()
        assertEquals(listOf("John Doe", "Szeged", "Hajós u. 28."), label.lines)
    }

    @Test
    fun `csak akkor teljes a címke, ha mindhárom mező ki van töltve`() {
        assertTrue(LabelData("John Doe", "Szeged", "Hajós u. 28.").isComplete)
        assertFalse(LabelData("John Doe", " ", "Hajós u. 28.").isComplete)
        assertFalse(LabelData("", "", "").isComplete)
    }
}
