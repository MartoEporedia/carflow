package com.carflow.app.data.repository

import com.carflow.app.data.dao.CategoryTotal
import com.carflow.app.data.dao.ExpenseDao
import com.carflow.app.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getByVehicle(vehicleId: String): Flow<List<ExpenseEntity>> =
        expenseDao.getByVehicle(vehicleId)

    fun getAllActive(): Flow<List<ExpenseEntity>> = expenseDao.getAllActive()

    fun getRecent(limit: Int = 10): Flow<List<ExpenseEntity>> = expenseDao.getRecent(limit)

    suspend fun getById(id: String): ExpenseEntity? = expenseDao.getById(id)

    suspend fun create(
        vehicleId: String,
        category: String,
        subcategory: String = "",
        amount: Double,
        quantity: Double? = null,
        quantityUnit: String? = null,
        description: String = "",
        date: Long = System.currentTimeMillis(),
        odometerKm: Double? = null,
        isFullTank: Boolean = false,
        gasStationName: String? = null,
        gasStationLocation: String? = null,
        pricePerLiter: Double? = null
    ): ExpenseEntity {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            vehicleId = vehicleId,
            category = category,
            subcategory = subcategory,
            amount = amount,
            quantity = quantity,
            quantityUnit = quantityUnit,
            description = description,
            date = date,
            odometerKm = odometerKm,
            isFullTank = isFullTank,
            gasStationName = gasStationName,
            gasStationLocation = gasStationLocation,
            pricePerLiter = pricePerLiter
        )
        expenseDao.insert(expense)
        return expense
    }

    suspend fun update(expense: ExpenseEntity) {
        expenseDao.update(expense.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: String) {
        expenseDao.softDelete(id)
    }

    fun getByDateRange(vehicleId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getByDateRange(vehicleId, startDate, endDate)

    suspend fun getTotalsByCategory(vehicleId: String, startDate: Long, endDate: Long): List<CategoryTotal> =
        expenseDao.getTotalsByCategory(vehicleId, startDate, endDate)

    suspend fun getTotalAmount(vehicleId: String, startDate: Long, endDate: Long): Double =
        expenseDao.getTotalAmount(vehicleId, startDate, endDate) ?: 0.0

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = getAllActive()

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insert(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        delete(expense.id)
    }
}
