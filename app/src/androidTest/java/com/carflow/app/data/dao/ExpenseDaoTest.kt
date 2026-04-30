package com.carflow.app.data.dao

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
class ExpenseDaoTest {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var db: CarFlowDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CarFlowDatabase::class.java
        ).build()
        expenseDao = db.expenseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadExpense() = runBlocking {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            vehicleId = "vehicle1",
            category = "FUEL",
            amount = 50.0,
            description = "Test fuel",
            date = System.currentTimeMillis()
        )
        expenseDao.insert(expense)
        val allExpenses = expenseDao.getAllActive().first()
        assertTrue(allExpenses.isNotEmpty())
        assertEquals(expense.id, allExpenses.first().id)
    }

    @Test
    @Throws(Exception::class)
    fun getAllExpensesReturnsEmptyWhenNoData() = runBlocking {
        val allExpenses = expenseDao.getAllActive().first()
        assertTrue(allExpenses.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteExpense() = runBlocking {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            vehicleId = "vehicle1",
            category = "MAINTENANCE",
            amount = 100.0,
            description = "Oil change"
        )
        expenseDao.insert(expense)
        val allExpenses = expenseDao.getAllActive().first()
        assertEquals(1, allExpenses.size)

        expenseDao.softDelete(expense.id)
        val afterDelete = expenseDao.getAllActive().first()
        assertTrue(afterDelete.isEmpty())
    }
}
