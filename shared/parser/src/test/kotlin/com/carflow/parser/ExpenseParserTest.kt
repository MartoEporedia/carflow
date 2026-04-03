package com.carflow.parser

import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.ParseConfidence
import com.carflow.parser.model.QuantityUnit
import com.carflow.parser.pipeline.Normalizer
import com.carflow.parser.pipeline.TokenExtractor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class ExpenseParserTest {

    private val parser = ExpenseParser()

    // ==========================================
    // FUEL EXPENSES
    // ==========================================
    @Nested
    inner class FuelExpenses {

        @Test
        fun `benzina with euro symbol`() {
            val result = parser.parse("benzina 50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("petrol")
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `benzina with euro symbol and liters`() {
            val result = parser.parse("benzina 50€ 30L")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("petrol")
            assertThat(result.amount).isEqualTo(50.0)
            assertThat(result.quantity).isEqualTo(30.0)
            assertThat(result.quantityUnit).isEqualTo(QuantityUnit.LITERS)
        }

        @Test
        fun `diesel with amount and liters`() {
            val result = parser.parse("diesel 40€ 30L")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("diesel")
            assertThat(result.amount).isEqualTo(40.0)
            assertThat(result.quantity).isEqualTo(30.0)
        }

        @Test
        fun `ricarica with kWh`() {
            val result = parser.parse("ricarica 20€ 15kWh")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("electric")
            assertThat(result.amount).isEqualTo(20.0)
            assertThat(result.quantity).isEqualTo(15.0)
            assertThat(result.quantityUnit).isEqualTo(QuantityUnit.KWH)
        }

        @Test
        fun `benzina with euro word and litri word`() {
            val result = parser.parse("benzina 50 euro 40 litri oggi")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("petrol")
            assertThat(result.amount).isEqualTo(50.0)
            assertThat(result.quantity).isEqualTo(40.0)
            assertThat(result.quantityUnit).isEqualTo(QuantityUnit.LITERS)
        }

        @Test
        fun `gasolio recognized as diesel`() {
            val result = parser.parse("gasolio 65€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("diesel")
            assertThat(result.amount).isEqualTo(65.0)
        }

        @Test
        fun `gpl recognized`() {
            val result = parser.parse("gpl 35€ 20L")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("lpg")
            assertThat(result.amount).isEqualTo(35.0)
            assertThat(result.quantity).isEqualTo(20.0)
        }

        @Test
        fun `metano recognized`() {
            val result = parser.parse("metano 25€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("cng")
            assertThat(result.amount).isEqualTo(25.0)
        }

        @Test
        fun `euro symbol before number`() {
            val result = parser.parse("benzina €50")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `fuel without currency symbol`() {
            val result = parser.parse("benzina 50")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `pieno recognized as fuel`() {
            val result = parser.parse("pieno 60€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(60.0)
        }

        @Test
        fun `rifornimento recognized as fuel`() {
            val result = parser.parse("rifornimento 45€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(45.0)
        }

        @Test
        fun `decimal amount with comma`() {
            val result = parser.parse("benzina 50,50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.50)
        }

        @Test
        fun `decimal amount with dot`() {
            val result = parser.parse("diesel 40.50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(40.50)
        }

        @Test
        fun `decimal quantity`() {
            val result = parser.parse("benzina 50€ 30,5L")
            assertThat(result.quantity).isEqualTo(30.5)
        }
    }

    // ==========================================
    // MAINTENANCE EXPENSES
    // ==========================================
    @Nested
    inner class MaintenanceExpenses {

        @Test
        fun `tagliando basic`() {
            val result = parser.parse("tagliando 200")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(200.0)
        }

        @Test
        fun `gomme with euro`() {
            val result = parser.parse("gomme 400€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(400.0)
        }

        @Test
        fun `revisione recognized`() {
            val result = parser.parse("revisione 80€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(80.0)
        }

        @Test
        fun `freni recognized`() {
            val result = parser.parse("freni 150€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(150.0)
        }

        @Test
        fun `cambio olio recognized`() {
            val result = parser.parse("cambio olio 50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `officina recognized`() {
            val result = parser.parse("officina 300€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(300.0)
        }

        @Test
        fun `lavaggio recognized`() {
            val result = parser.parse("lavaggio 10€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(10.0)
        }

        @Test
        fun `english maintenance keywords`() {
            val result = parser.parse("oil change 45€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
        }

        @Test
        fun `batteria recognized`() {
            val result = parser.parse("batteria 120€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(120.0)
        }
    }

    // ==========================================
    // EXTRA EXPENSES
    // ==========================================
    @Nested
    inner class ExtraExpenses {

        @Test
        fun `assicurazione basic`() {
            val result = parser.parse("assicurazione 500")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(500.0)
        }

        @Test
        fun `bollo recognized`() {
            val result = parser.parse("bollo 250€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(250.0)
        }

        @Test
        fun `parcheggio recognized`() {
            val result = parser.parse("parcheggio 5€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(5.0)
        }

        @Test
        fun `multa recognized`() {
            val result = parser.parse("multa 100€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(100.0)
        }

        @Test
        fun `pedaggio recognized`() {
            val result = parser.parse("pedaggio 15€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(15.0)
        }

        @Test
        fun `telepass recognized`() {
            val result = parser.parse("telepass 30€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(30.0)
        }

        @Test
        fun `english extra keywords`() {
            val result = parser.parse("parking 3€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(3.0)
        }

        @Test
        fun `insurance english`() {
            val result = parser.parse("insurance 600€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(600.0)
        }
    }

    // ==========================================
    // EDGE CASES
    // ==========================================
    @Nested
    inner class EdgeCases {

        @Test
        fun `empty input`() {
            val result = parser.parse("")
            assertThat(result.category).isEqualTo(ExpenseCategory.UNKNOWN)
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
            assertThat(result.warnings).contains("Empty input")
        }

        @Test
        fun `whitespace only`() {
            val result = parser.parse("   ")
            assertThat(result.category).isEqualTo(ExpenseCategory.UNKNOWN)
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
        }

        @Test
        fun `no category keyword`() {
            val result = parser.parse("50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.UNKNOWN)
            assertThat(result.amount).isEqualTo(50.0)
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
        }

        @Test
        fun `no amount`() {
            val result = parser.parse("benzina")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isNull()
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
        }

        @Test
        fun `uppercase input`() {
            val result = parser.parse("BENZINA 50€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `mixed case input`() {
            val result = parser.parse("Benzina 50€ 30L")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
            assertThat(result.quantity).isEqualTo(30.0)
        }

        @Test
        fun `extra spaces`() {
            val result = parser.parse("  benzina   50 €   30 L  ")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `typo in benzina`() {
            // The normalizer should not handle all typos, but should handle common ones
            val result = parser.parse("taglando 150€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(150.0)
        }

        @Test
        fun `typo in assicurazione`() {
            val result = parser.parse("asicurazione 500€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(500.0)
        }
    }

    // ==========================================
    // NORMALIZER TESTS
    // ==========================================
    @Nested
    inner class NormalizerTests {

        @Test
        fun `lowercase conversion`() {
            val result = Normalizer.normalize("BENZINA 50€")
            assertThat(result).startsWith("benzina")
        }

        @Test
        fun `euro word to symbol`() {
            val result = Normalizer.normalize("benzina 50 euro")
            assertThat(result).contains("€")
        }

        @Test
        fun `litri to L`() {
            val result = Normalizer.normalize("30 litri")
            assertThat(result).contains("L")
        }

        @Test
        fun `multiple spaces collapsed`() {
            val result = Normalizer.normalize("benzina   50   euro")
            assertThat(result).doesNotContain("  ")
        }

        @Test
        fun `typo corrections applied`() {
            assertThat(Normalizer.normalize("taglando")).contains("tagliando")
            assertThat(Normalizer.normalize("revissione")).contains("revisione")
        }
    }

    // ==========================================
    // TOKEN EXTRACTOR TESTS
    // ==========================================
    @Nested
    inner class TokenExtractorTests {

        @Test
        fun `extract amount with euro symbol after`() {
            val tokens = TokenExtractor.extract("benzina 50 €")
            assertThat(tokens.amounts).contains(50.0)
        }

        @Test
        fun `extract amount with euro symbol before`() {
            val tokens = TokenExtractor.extract("benzina € 50")
            assertThat(tokens.amounts).contains(50.0)
        }

        @Test
        fun `extract liters`() {
            val tokens = TokenExtractor.extract("benzina 50 € 30L")
            assertThat(tokens.quantities).hasSize(1)
            assertThat(tokens.quantities[0].value).isEqualTo(30.0)
            assertThat(tokens.quantities[0].unit).isEqualTo(QuantityUnit.LITERS)
        }

        @Test
        fun `extract kWh`() {
            val tokens = TokenExtractor.extract("ricarica 20 € 15kWh")
            assertThat(tokens.quantities).hasSize(1)
            assertThat(tokens.quantities[0].value).isEqualTo(15.0)
            assertThat(tokens.quantities[0].unit).isEqualTo(QuantityUnit.KWH)
        }

        @Test
        fun `extract words`() {
            val tokens = TokenExtractor.extract("benzina 50 €")
            assertThat(tokens.words).contains("benzina")
        }

        @Test
        fun `parse european number format`() {
            assertThat(TokenExtractor.parseNumber("1.234,56")).isEqualTo(1234.56)
            assertThat(TokenExtractor.parseNumber("50,50")).isEqualTo(50.50)
            assertThat(TokenExtractor.parseNumber("50.50")).isEqualTo(50.50)
            assertThat(TokenExtractor.parseNumber("50")).isEqualTo(50.0)
        }
    }

    // ==========================================
    // CONFIDENCE TESTS
    // ==========================================
    @Nested
    inner class ConfidenceTests {

        @Test
        fun `high confidence for clear input`() {
            val result = parser.parse("benzina 50€")
            assertThat(result.confidence).isEqualTo(ParseConfidence.HIGH)
        }

        @Test
        fun `low confidence for no category`() {
            val result = parser.parse("50€")
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
        }

        @Test
        fun `low confidence for no amount`() {
            val result = parser.parse("benzina")
            assertThat(result.confidence).isEqualTo(ParseConfidence.LOW)
        }
    }

    // ==========================================
    // EXTENSIBILITY TESTS
    // ==========================================
    @Nested
    inner class ExtensibilityTests {

        @Test
        fun `add custom keywords`() {
            val customParser = ExpenseParser()
            customParser.getDictionary().addKeywords(
                ExpenseCategory.EXTRA,
                listOf("noleggio", "rental")
            )
            val result = customParser.parse("noleggio 100€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(100.0)
        }
    }

    // ==========================================
    // REAL-WORLD MESSY INPUT
    // ==========================================
    @Nested
    inner class RealWorldInput {

        @Test
        fun `natural Italian sentence - fuel`() {
            val result = parser.parse("ho fatto benzina 50 euro 40 litri oggi")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
            assertThat(result.quantity).isEqualTo(40.0)
        }

        @Test
        fun `short telegram style - diesel`() {
            val result = parser.parse("diesel 40€ 30L")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("diesel")
            assertThat(result.amount).isEqualTo(40.0)
            assertThat(result.quantity).isEqualTo(30.0)
        }

        @Test
        fun `maintenance with description`() {
            val result = parser.parse("tagliando 200 euro alla Ford")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(200.0)
        }

        @Test
        fun `insurance annual`() {
            val result = parser.parse("assicurazione annuale 500€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(500.0)
        }

        @Test
        fun `parking quick`() {
            val result = parser.parse("parcheggio 2€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(2.0)
        }

        @Test
        fun `electric charging`() {
            val result = parser.parse("ricarica elettrica 15€ 10kWh")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.subcategory).isEqualTo("electric")
            assertThat(result.amount).isEqualTo(15.0)
            assertThat(result.quantity).isEqualTo(10.0)
            assertThat(result.quantityUnit).isEqualTo(QuantityUnit.KWH)
        }

        @Test
        fun `toll highway`() {
            val result = parser.parse("autostrada 12€")
            assertThat(result.category).isEqualTo(ExpenseCategory.EXTRA)
            assertThat(result.amount).isEqualTo(12.0)
        }

        @Test
        fun `tires replacement`() {
            val result = parser.parse("pneumatici invernali 600€")
            assertThat(result.category).isEqualTo(ExpenseCategory.MAINTENANCE)
            assertThat(result.amount).isEqualTo(600.0)
        }

        @Test
        fun `simple number only with keyword`() {
            val result = parser.parse("benzina 50")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isEqualTo(50.0)
        }

        @Test
        fun `amount with cents comma format`() {
            val result = parser.parse("benzina 50,99€")
            assertThat(result.category).isEqualTo(ExpenseCategory.FUEL)
            assertThat(result.amount).isCloseTo(50.99, org.assertj.core.data.Offset.offset(0.01))
        }
    }
}
