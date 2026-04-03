package com.carflow.parser

import com.carflow.parser.keywords.KeywordDictionary
import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.ParseConfidence
import com.carflow.parser.model.ParsedExpense
import com.carflow.parser.pipeline.CategoryClassifier
import com.carflow.parser.pipeline.DateParser
import com.carflow.parser.pipeline.Normalizer
import com.carflow.parser.pipeline.TokenExtractor

/**
 * Main entry point for natural language expense parsing.
 *
 * Processes free-text input through a deterministic pipeline:
 * 1. Normalization (lowercase, fix typos, standardize symbols)
 * 2. Token extraction (amounts, quantities, words)
 * 3. Category classification (keyword matching)
 * 4. Structured output assembly
 *
 * Works fully offline - no external APIs required.
 *
 * Usage:
 * ```
 * val parser = ExpenseParser()
 * val result = parser.parse("benzina 50€ 30L")
 * // result.category == FUEL
 * // result.subcategory == "petrol"
 * // result.amount == 50.0
 * // result.quantity == 30.0
 * ```
 */
class ExpenseParser(
    private val dictionary: KeywordDictionary = KeywordDictionary()
) {
    private val classifier = CategoryClassifier(dictionary)

    fun parse(input: String): ParsedExpense {
        if (input.isBlank()) {
            return ParsedExpense(
                category = ExpenseCategory.UNKNOWN,
                confidence = ParseConfidence.LOW,
                warnings = listOf("Empty input")
            )
        }

        // Stage 1: Normalize
        val normalized = Normalizer.normalize(input)

        // Stage 2: Extract tokens
        val tokens = TokenExtractor.extract(normalized)

        // Stage 3: Classify
        val classification = classifier.classify(tokens.words)

        // Stage 4: Parse date
        val dateResult = DateParser.parse(tokens.words, normalized)

        // Stage 5: Assemble result
        val warnings = mutableListOf<String>()
        var confidence = ParseConfidence.HIGH

        // Determine amount
        val amount = when {
            tokens.amounts.isEmpty() -> {
                warnings.add("No amount detected")
                confidence = ParseConfidence.LOW
                null
            }
            tokens.amounts.size == 1 -> tokens.amounts.first()
            else -> {
                // Multiple numbers: first is usually the amount
                // But if we have quantities, the non-quantity number is the amount
                if (tokens.quantities.isNotEmpty()) {
                    // The amount is the number NOT matched as a quantity
                    tokens.amounts.first()
                } else {
                    warnings.add("Multiple amounts detected, using first: ${tokens.amounts.first()}")
                    confidence = minOf(confidence, ParseConfidence.MEDIUM)
                    tokens.amounts.first()
                }
            }
        }

        // Category confidence
        if (classification.category == ExpenseCategory.UNKNOWN) {
            warnings.add("Category could not be determined")
            confidence = minOf(confidence, ParseConfidence.LOW)
        } else if (classification.score < 0.8) {
            confidence = minOf(confidence, ParseConfidence.MEDIUM)
        }

        // Build description from remaining words (exclude matched keyword and date)
        val descriptionWords = tokens.words.filter { word ->
            word != classification.matchedKeyword &&
            word != dateResult.matchedText &&
            !isStopWord(word)
        }

        return ParsedExpense(
            category = classification.category,
            subcategory = classification.subcategory,
            amount = amount,
            quantity = tokens.quantities.firstOrNull()?.value,
            quantityUnit = tokens.quantities.firstOrNull()?.unit,
            description = descriptionWords.joinToString(" ").trim(),
            rawInput = input,
            date = dateResult.parsedDate,
            confidence = confidence,
            warnings = warnings
        )
    }

    private fun isStopWord(word: String): Boolean {
        return word in setOf(
            "di", "il", "la", "lo", "le", "i", "gli", "un", "una", "uno",
            "del", "della", "dello", "dei", "delle", "degli",
            "per", "con", "su", "in", "da", "a", "al", "alla", "allo",
            "the", "a", "an", "of", "for", "with", "on", "in", "at", "to"
        )
    }

    /**
     * Returns the keyword dictionary for extension.
     */
    fun getDictionary(): KeywordDictionary = dictionary

    companion object {
        private fun minOf(a: ParseConfidence, b: ParseConfidence): ParseConfidence {
            return if (a.ordinal > b.ordinal) a else b
        }
    }
}
