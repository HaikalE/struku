package com.example.struku.domain.usecase

import com.example.struku.domain.model.ReceiptWithItems
import com.example.struku.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Use case for getting receipts with various filtering options
 */
class GetReceiptsUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository
) {
    /**
     * Get all receipts
     */
    fun getAllReceipts(): Flow<List<ReceiptWithItems>> {
        return receiptRepository.getAllReceiptsWithItems()
    }

    /**
     * Get receipts for the current month
     */
    fun getCurrentMonthReceipts(): Flow<List<ReceiptWithItems>> {
        val calendar = Calendar.getInstance()

        // Set to first day of current month
        val startDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Set to last day of current month
        val endDate = calendar.apply {
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return receiptRepository.getReceiptsForDateRange(startDate, endDate)
    }

    /**
     * Get receipts for a specific category
     */
    fun getReceiptsByCategory(categoryId: Long): Flow<List<ReceiptWithItems>> {
        return receiptRepository.getReceiptsForCategory(categoryId)
    }

    /**
     * Get receipts for a custom date range
     */
    fun getReceiptsByDateRange(startDate: Date, endDate: Date): Flow<List<ReceiptWithItems>> {
        return receiptRepository.getReceiptsForDateRange(startDate, endDate)
    }

    /**
     * Get a single receipt by ID
     */
    suspend fun getReceiptById(receiptId: Long): ReceiptWithItems? {
        return receiptRepository.getReceiptWithItems(receiptId)
    }
}