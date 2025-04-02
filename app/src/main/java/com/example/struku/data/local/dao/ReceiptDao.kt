package com.example.struku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.struku.data.model.Receipt
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: Receipt): Long

    @Update
    suspend fun update(receipt: Receipt)

    @Delete
    suspend fun delete(receipt: Receipt)

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllReceiptsFlow(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Long): Receipt?

    @Query("SELECT * FROM receipts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getReceiptsByDateRange(startDate: Date, endDate: Date): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getReceiptsByCategory(categoryId: Long): Flow<List<Receipt>>

    @Query("SELECT SUM(totalAmount) FROM receipts WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpendingForPeriod(startDate: Date, endDate: Date): Double?
}