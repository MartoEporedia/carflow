package com.carflow.parser.pipeline

import java.util.*
import kotlin.math.abs


/**
 * Extracts date references from text.
 * Returns the relative day offset (0 = today, -1 = yesterday, etc.)
 */
object DateParser {

    data class DateResult(
        val dayOffset: Int,
        val matchedText: String?,
        val parsedDate: Long? = null
    )

    private val dateKeywords = mapOf(
        "oggi" to 0,
        "today" to 0,
        "ieri" to -1,
        "yesterday" to -1,
        "l'altro ieri" to -2,
        "day before yesterday" to -2,
        "stamattina" to 0,
        "stasera" to 0,
        "this morning" to 0,
        "tonight" to 0,
    )

    fun parse(words: List<String>, rawText: String): DateResult {
        // Check multi-word expressions first
        for ((keyword, offset) in dateKeywords) {
            if (keyword.contains(" ")) {
                if (rawText.contains(keyword, ignoreCase = true)) {
                    return DateResult(offset, keyword, calculateDate(offset))
                }
            }
        }

        // Check single word keywords
        for (word in words) {
            dateKeywords[word.lowercase()]?.let { offset ->
                return DateResult(offset, word, calculateDate(offset))
            }
        }

        return DateResult(0, null, null) // Default to today
    }

    private fun calculateDate(dayOffset: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, dayOffset)
        // Reset to start of day? For now use exact timestamp
        return calendar.timeInMillis
    }
}
