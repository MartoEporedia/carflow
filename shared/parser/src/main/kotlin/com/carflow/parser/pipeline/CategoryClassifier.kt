package com.carflow.parser.pipeline

import com.carflow.parser.keywords.KeywordDictionary
import com.carflow.parser.model.ExpenseCategory

/**
 * Third stage: classifies expense category based on extracted words.
 */
class CategoryClassifier(private val dictionary: KeywordDictionary) {

    data class Classification(
        val category: ExpenseCategory,
        val subcategory: String? = null,
        val matchedKeyword: String? = null,
        val score: Double = 0.0
    )

    fun classify(words: List<String>): Classification {
        var bestCategory = ExpenseCategory.UNKNOWN
        var bestScore = 0.0
        var matchedKeyword: String? = null
        var detectedFuelType: FuelType? = null

        for (word in words) {
            val category = dictionary.getCategoryExact(word)
            if (category != ExpenseCategory.UNKNOWN) {
                val score = 1.0
                if (score > bestScore) {
                    bestScore = score
                    bestCategory = category
                    matchedKeyword = word
                }
            } else {
                val partialCategory = dictionary.getCategory(word)
                if (partialCategory != ExpenseCategory.UNKNOWN) {
                    val score = 0.7
                    if (score > bestScore) {
                        bestScore = score
                        bestCategory = partialCategory
                        matchedKeyword = word
                    }
                }
            }

            if (detectedFuelType == null) {
                detectedFuelType = dictionary.getFuelType(word)
            }
        }

        // Multi-word matching: try bigrams
        if (bestCategory == ExpenseCategory.UNKNOWN && words.size >= 2) {
            for (i in 0 until words.size - 1) {
                val bigram = "${words[i]} ${words[i + 1]}"
                val category = dictionary.getCategory(bigram)
                if (category != ExpenseCategory.UNKNOWN) {
                    bestCategory = category
                    bestScore = 0.8
                    matchedKeyword = bigram
                    break
                }
            }
        }

        val subcategory = if (bestCategory == ExpenseCategory.FUEL) detectedFuelType?.name?.lowercase() else null

        return Classification(
            category = bestCategory,
            subcategory = subcategory,
            matchedKeyword = matchedKeyword,
            score = bestScore
        )
    }
}
