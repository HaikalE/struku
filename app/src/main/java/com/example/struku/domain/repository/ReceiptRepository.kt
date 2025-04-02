package com.example.struku.domain.repository


import com.example.struku.domain.model.ReceiptWithItems
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for managing receipts and their line items
 */
interface ReceiptRepository {
    /**
     * Get all receipts with their line items as a Flow
     */
    fun getAllReceiptsWithItems(): Flow<List<ReceiptWithItems>>

    /**
     * Get a specific receipt with its line items
     */
    suspend fun getReceiptWithItems(receiptId: Long): ReceiptWithItems?

    /**
     * Save a receipt with its line items
     */
    suspend fun saveReceiptWithItems(receiptWithItems: ReceiptWithItems): Long

    /**
     * Delete a receipt and all its line items
     */
    suspend fun deleteReceipt(receiptId: Long)

    /**
     * Get receipts for a specific date range
     */
    fun getReceiptsForDateRange(startDate: Date, endDate: Date): Flow<List<ReceiptWithItems>>

    /**
     * Get receipts for a specific category
     */
    fun getReceiptsForCategory(categoryId: Long): Flow<List<ReceiptWithItems>>

    /**
     * Get total spending for a time period
     */
    suspend fun getTotalSpendingForPeriod(startDate: Date, endDate: Date): Double
}