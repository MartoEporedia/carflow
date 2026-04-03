package com.carflow.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carflow.app.data.database.CarFlowDatabase
import com.carflow.app.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class ExpenseRepositoryTest {

    private lateinit var db: CarFlowDatabase
    private lateinit var expenseRepository: ExpenseRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CarFlowDatabase::class.java
        ).build()
        expenseRepository = ExpenseRepository(db.expenseDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndRetrieveExpense() = runBlocking {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            vehicleId = "v1",
            category = "FUEL",
            amount = 45.5,
            description = "Test"
        )
        expenseRepository.insertExpense(expense)
        val result = expenseRepository.getAllExpenses().first()
        assertEquals(1, result.size)
        assertEquals(expense.id, result.first().id)
    }

    @Test
    @Throws(Exception::class)
    fun deleteExpense() = runBlocking {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            vehicleId = "v1",
            category = "EXTRA",
            amount = 10.0
        )
        expenseRepository.insertExpense(expense)
        var all = expenseRepository.getAllExpenses().first()
        assertEquals(1, all.size)

        expenseRepository.deleteExpense(expense)
        all = expenseRepository.getAllExpenses().first()
        assertTrue(all.isEmpty())
    }
}
