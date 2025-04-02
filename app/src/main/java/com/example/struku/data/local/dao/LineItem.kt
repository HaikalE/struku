package com.example.struku.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.struku.data.model.LineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LineItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lineItem: LineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lineItems: List<LineItem>)

    @Update
    suspend fun update(lineItem: LineItem)

    @Delete
    suspend fun delete(lineItem: LineItem)

    @Query("SELECT * FROM line_items WHERE receiptId = :receiptId")
    fun getLineItemsForReceipt(receiptId: Long): Flow<List<LineItem>>

    @Query("DELETE FROM line_items WHERE receiptId = :receiptId")
    suspend fun deleteAllForReceipt(receiptId: Long)

    @Query("SELECT * FROM line_items WHERE id = :id")
    suspend fun getLineItemById(id: Long): LineItem?

    @Query("SELECT SUM(total) FROM line_items WHERE receiptId = :receiptId")
    suspend fun getTotalForReceipt(receiptId: Long): Double?
}