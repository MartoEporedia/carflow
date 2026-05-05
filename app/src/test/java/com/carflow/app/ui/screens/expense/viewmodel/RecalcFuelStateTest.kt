package com.carflow.app.ui.screens.expense.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class RecalcFuelStateTest {

    private fun state(
        total: String = "",
        price: String = "",
        liters: String = ""
    ) = FuelFormState(totalPrice = total, pricePerLiter = price, liters = liters)

    // --- Auto-calculation: exactly two valid inputs fill the third ---

    @Test
    fun `given total and price, calculates liters`() {
        val result = recalcFuelState(state(total = "50.0", price = "1.80"))
        assertEquals("27.78", result.liters)
        assertEquals("50.0", result.totalPrice)
        assertEquals("1.80", result.pricePerLiter)
    }

    @Test
    fun `given total and liters, calculates price per liter`() {
        val result = recalcFuelState(state(total = "54.0", liters = "30.0"))
        assertEquals("1.80", result.pricePerLiter)
        assertEquals("54.0", result.totalPrice)
        assertEquals("30.0", result.liters)
    }

    @Test
    fun `given price and liters, calculates total`() {
        val result = recalcFuelState(state(price = "1.80", liters = "30.0"))
        assertEquals("54.00", result.totalPrice)
        assertEquals("1.80", result.pricePerLiter)
        assertEquals("30.0", result.liters)
    }

    // --- No auto-calculation: fewer than two valid inputs ---

    @Test
    fun `given only total, state is unchanged`() {
        val input = state(total = "50.0")
        assertEquals(input, recalcFuelState(input))
    }

    @Test
    fun `given only price, state is unchanged`() {
        val input = state(price = "1.80")
        assertEquals(input, recalcFuelState(input))
    }

    @Test
    fun `given only liters, state is unchanged`() {
        val input = state(liters = "30.0")
        assertEquals(input, recalcFuelState(input))
    }

    @Test
    fun `given empty state, state is unchanged`() {
        val input = state()
        assertEquals(input, recalcFuelState(input))
    }

    // --- Edge cases ---

    @Test
    fun `given all three valid inputs, state is unchanged`() {
        val input = state(total = "54.0", price = "1.80", liters = "30.0")
        assertEquals(input, recalcFuelState(input))
    }

    @Test
    fun `given zero total, treats as invalid and does not divide by zero`() {
        val input = state(total = "0", price = "1.80")
        assertEquals(input, recalcFuelState(input))
    }

    @Test
    fun `given non-numeric input, state is unchanged`() {
        val input = state(total = "abc", price = "1.80")
        assertEquals(input, recalcFuelState(input))
    }
}
