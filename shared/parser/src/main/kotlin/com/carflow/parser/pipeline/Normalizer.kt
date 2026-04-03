package com.carflow.parser.pipeline

/**
 * First stage of the parsing pipeline.
 * Normalizes input text for consistent processing.
 */
object Normalizer {

    private val multipleSpaces = Regex("\\s+")
    private val currencySymbolVariations = mapOf(
        "€" to " € ",
        "eur" to " € ",
        "euro" to " € ",
        "euros" to " € ",
    )
    // Common OCR or typo corrections
    private val corrections = mapOf(
        "benzìna" to "benzina",
        "diesèl" to "diesel",
        "diessel" to "diesel",
        "gazolio" to "gasolio",
        "taglando" to "tagliando",
        "revissione" to "revisione",
        "asicurazione" to "assicurazione",
        "assicurazzione" to "assicurazione",
        "parchegio" to "parcheggio",
    )

    fun normalize(input: String): String {
        var text = input.trim().lowercase()

        // Apply typo corrections
        for ((wrong, right) in corrections) {
            text = text.replace(wrong, right)
        }

        // Normalize currency symbols - but carefully handle "euro" as standalone word
        text = text.replace("€", " € ")
        // Replace "euro" / "eur" only as whole words to avoid mangling other words
        text = text.replace(Regex("\\b(euros?)\\b"), " € ")
        text = text.replace(Regex("\\beur\\b"), " € ")

        // Normalize unit variations
        text = text.replace(Regex("\\blitri\\b"), "L")
        text = text.replace(Regex("\\blitro\\b"), "L")
        text = text.replace(Regex("\\bliters?\\b"), "L")
        text = text.replace(Regex("\\bkwh\\b", RegexOption.IGNORE_CASE), "kWh")

        // Normalize "oggi" (today), "ieri" (yesterday)
        // These are kept as-is for the date parser to handle

        // Clean up multiple spaces
        text = text.replace(multipleSpaces, " ").trim()

        return text
    }
}
