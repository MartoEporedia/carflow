package com.carflow.app.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carflow.app.data.database.CarFlowDatabase
import com.carflow.app.data.entity.VehicleEntity
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
class VehicleDaoTest {

    private lateinit var vehicleDao: VehicleDao
    private lateinit var db: CarFlowDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            CarFlowDatabase::class.java
        ).build()
        vehicleDao = db.vehicleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadVehicle() = runBlocking {
        val vehicle = VehicleEntity(
            id = UUID.randomUUID().toString(),
            name = "Fiat 500",
            make = "Fiat",
            model = "500",
            licensePlate = "AB123CD"
        )
        vehicleDao.insert(vehicle)
        val allVehicles = vehicleDao.getAllActive().first()
        assertTrue(allVehicles.isNotEmpty())
        assertEquals(vehicle.id, allVehicles.first().id)
    }

    @Test
    @Throws(Exception::class)
    fun getAllVehiclesReturnsEmptyWhenNoData() = runBlocking {
        val allVehicles = vehicleDao.getAllActive().first()
        assertTrue(allVehicles.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteVehicle() = runBlocking {
        val vehicle = VehicleEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Car"
        )
        vehicleDao.insert(vehicle)
        var allVehicles = vehicleDao.getAllActive().first()
        assertEquals(1, allVehicles.size)

        vehicleDao.softDelete(vehicle.id)
        allVehicles = vehicleDao.getAllActive().first()
        assertTrue(allVehicles.isEmpty())
    }
}
