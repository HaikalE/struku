package com.example.struku.data.repository

import com.example.struku.data.local.dao.CategoryDao
import com.example.struku.data.local.dao.LineItemDao
import com.example.struku.data.local.dao.ReceiptDao
import com.example.struku.data.model.LineItem
import com.example.struku.data.model.Receipt
import com.example.struku.domain.model.LineItemModel
import com.example.struku.domain.model.ReceiptWithItems
import com.example.struku.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class ReceiptRepositoryImpl @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val lineItemDao: LineItemDao,
    private val categoryDao: CategoryDao
) : ReceiptRepository {

    override fun getAllReceiptsWithItems(): Flow<List<ReceiptWithItems>> {
        return receiptDao.getAllReceiptsFlow().map { receipts ->
            receipts.map { receipt ->
                mapToReceiptWithItems(receipt)
            }
        }
    }

    override suspend fun getReceiptWithItems(receiptId: Long): ReceiptWithItems? {
        val receipt = receiptDao.getReceiptById(receiptId) ?: return null
        return mapToReceiptWithItems(receipt)
    }

    override suspend fun saveReceiptWithItems(receiptWithItems: ReceiptWithItems): Long {
        // Map domain models to entities
        val receipt = Receipt(
            id = receiptWithItems.id,
            storeName = receiptWithItems.storeName,
            totalAmount = receiptWithItems.totalAmount,
            date = receiptWithItems.date,
            categoryId = receiptWithItems.categoryId,
            notes = receiptWithItems.notes,
            imagePath = receiptWithItems.imagePath,
            rawOcrText = null, // Add OCR text if needed
            createdAt = receiptWithItems.createdAt,
            updatedAt = Date() // Update to current time
        )

        // Save receipt first to get ID if new
        val receiptId = receiptDao.insert(receipt)

        // Delete existing line items and re-add them
        lineItemDao.deleteAllForReceipt(receiptId)

        // Save all line items
        val lineItems = receiptWithItems.lineItems.map { item ->
            LineItem(
                id = item.id,
                receiptId = receiptId,
                name = item.name,
                quantity = item.quantity,
                price = item.price,
                total = item.total,
                categoryId = item.categoryId
            )
        }
        lineItemDao.insertAll(lineItems)

        return receiptId
    }

    override suspend fun deleteReceipt(receiptId: Long) {
        val receipt = receiptDao.getReceiptById(receiptId) ?: return
        receiptDao.delete(receipt)
        // Line items will be deleted automatically due to ForeignKey constraint with CASCADE
    }

    override fun getReceiptsForDateRange(startDate: Date, endDate: Date): Flow<List<ReceiptWithItems>> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate).map { receipts ->
            receipts.map { receipt ->
                mapToReceiptWithItems(receipt)
            }
        }
    }

    override fun getReceiptsForCategory(categoryId: Long): Flow<List<ReceiptWithItems>> {
        return receiptDao.getReceiptsByCategory(categoryId).map { receipts ->
            receipts.map { receipt ->
                mapToReceiptWithItems(receipt)
            }
        }
    }

    override suspend fun getTotalSpendingForPeriod(startDate: Date, endDate: Date): Double {
        return receiptDao.getTotalSpendingForPeriod(startDate, endDate) ?: 0.0
    }

    // Helper method to map a Receipt entity to ReceiptWithItems domain model
    private suspend fun mapToReceiptWithItems(receipt: Receipt): ReceiptWithItems {
        // Get all line items for this receipt
        val lineItems = lineItemDao.getLineItemsForReceipt(receipt.id).map { items ->
            items.map { item ->
                val category = item.categoryId?.let { categoryDao.getCategoryById(it) }
                LineItemModel(
                    id = item.id,
                    name = item.name,
                    quantity = item.quantity,
                    price = item.price,
                    total = item.total,
                    categoryId = item.categoryId,
                    categoryName = category?.name
                )
            }
        }.first() // Get first emission from Flow

        // Get category info if present
        val category = receipt.categoryId?.let { categoryDao.getCategoryById(it) }

        return ReceiptWithItems(
            id = receipt.id,
            storeName = receipt.storeName,
            totalAmount = receipt.totalAmount,
            date = receipt.date,
            categoryId = receipt.categoryId,
            categoryName = category?.name,
            categoryColor = category?.color,
            notes = receipt.notes,
            imagePath = receipt.imagePath,
            lineItems = lineItems,
            createdAt = receipt.createdAt,
            updatedAt = receipt.updatedAt
        )
    }
}