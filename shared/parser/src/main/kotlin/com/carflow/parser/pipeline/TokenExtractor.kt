package com.carflow.parser.pipeline

import com.carflow.parser.model.QuantityUnit

/**
 * Second stage: extracts structured tokens from normalized text.
 */
object TokenExtractor {

    data class ExtractedTokens(
        val amounts: List<Double>,
        val quantities: List<QuantityWithUnit>,
        val words: List<String>,
        val rawText: String
    )

    data class QuantityWithUnit(
        val value: Double,
        val unit: QuantityUnit
    )

    // Matches numbers like: 50, 50.5, 50,5, 1.234,56, 1234.56
    private val numberPattern = Regex("""(\d{1,3}(?:[.]\d{3})*(?:[,]\d{1,2})?|\d+(?:[.,]\d{1,2})?)""")

    // Amount patterns: number followed by € or preceded by €
    // "50€", "€50", "50 €", "€ 50", just "50" (if context suggests amount)
    private val amountWithCurrency = Regex("""(\d+(?:[.,]\d{1,2})?)\s*€|€\s*(\d+(?:[.,]\d{1,2})?)""")

    // Quantity patterns: number followed by L or kWh
    private val quantityLiters = Regex("""(\d+(?:[.,]\d{1,2})?)\s*[lL]\b""")
    private val quantityKwh = Regex("""(\d+(?:[.,]\d{1,2})?)\s*kWh""")

    fun extract(normalizedText: String): ExtractedTokens {
        val amounts = mutableListOf<Double>()
        val quantities = mutableListOf<QuantityWithUnit>()

        // Extract amounts with currency
        val amountMatches = amountWithCurrency.findAll(normalizedText)
        val amountRanges = mutableListOf<IntRange>()
        for (match in amountMatches) {
            val valueStr = match.groupValues[1].ifEmpty { match.groupValues[2] }
            parseNumber(valueStr)?.let { amounts.add(it) }
            amountRanges.add(match.range)
        }

        // Extract quantities with units
        val quantityRanges = mutableListOf<IntRange>()

        quantityLiters.findAll(normalizedText).forEach { match ->
            parseNumber(match.groupValues[1])?.let {
                quantities.add(QuantityWithUnit(it, QuantityUnit.LITERS))
            }
            quantityRanges.add(match.range)
        }

        quantityKwh.findAll(normalizedText).forEach { match ->
            parseNumber(match.groupValues[1])?.let {
                quantities.add(QuantityWithUnit(it, QuantityUnit.KWH))
            }
            quantityRanges.add(match.range)
        }

        // Extract remaining words (non-numeric, non-unit, non-currency tokens)
        val allMatchedRanges = amountRanges + quantityRanges
        var remainingText = normalizedText
        // Remove matched numeric tokens to get descriptive words
        // We rebuild the word list from tokens that aren't purely numeric or currency
        val words = remainingText.split(Regex("\\s+"))
            .filter { word ->
                word.isNotBlank() &&
                word != "€" &&
                !word.matches(Regex("""\d+([.,]\d+)?""")) &&
                word.lowercase() != "kwh" &&
                !(word.length == 1 && word.lowercase() == "l")
            }

        // If no amount with currency was found, look for standalone numbers as potential amounts
        if (amounts.isEmpty()) {
            val standaloneNumbers = numberPattern.findAll(normalizedText)
                .filter { match ->
                    // Check this number isn't already captured as a quantity
                    quantityRanges.none { qr -> match.range.first in qr || qr.first in match.range }
                }
                .mapNotNull { parseNumber(it.value) }
                .toList()
            amounts.addAll(standaloneNumbers)
        }

        return ExtractedTokens(
            amounts = amounts,
            quantities = quantities,
            words = words,
            rawText = normalizedText
        )
    }

    fun parseNumber(value: String): Double? {
        if (value.isBlank()) return null
        // Handle European format: 1.234,56 -> 1234.56
        val cleaned = if (value.contains(",") && value.contains(".")) {
            value.replace(".", "").replace(",", ".")
        } else if (value.contains(",")) {
            value.replace(",", ".")
        } else {
            value
        }
        return cleaned.toDoubleOrNull()
    }
}
