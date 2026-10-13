package com.example.labelprinter.model

/**
 * A címkére kerülő adatok: név, város, utca + házszám.
 */
data class LabelData(
    val name: String,
    val city: String,
    val street: String,
) {
    /** A címke sorai fentről lefelé, pontosan ebben a sorrendben nyomtatódnak. */
    val lines: List<String> get() = listOf(name, city, street)

    /** Igaz, ha mindhárom mező ki van töltve – csak ilyenkor nyomtatható. */
    val isComplete: Boolean get() = lines.none { it.isBlank() }

    /** Nyomtatáshoz előkészített példány: a sorok elején/végén lévő szóközök nélkül. */
    fun trimmed(): LabelData = LabelData(name.trim(), city.trim(), street.trim())

    companion object {
        /** Egy mező legfeljebb ennyi karakter lehet. */
        const val MAX_LENGTH = 32

        /** Kezdőértékek, hogy induláskor is legyen mit megnézni az előnézetben. */
        val Sample = LabelData(
            name = "",
            city = "",
            street = "",
        )

        /**
         * Beviteli mező értékének tisztítása: a sortöréseket (pl. beillesztésből)
         * szóközre cseréli, és [MAX_LENGTH] karakterre vágja, mert minden mező
         * pontosan egy sor a címkén.
         */
        fun sanitize(input: String): String =
            input.replace(Regex("[\\r\\n\\t]+"), " ").take(MAX_LENGTH)
    }
}
