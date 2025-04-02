package com.example.struku.domain.model

import java.util.Date

/**
 * Domain model representing a receipt with all its line items
 */
data class ReceiptWithItems(
    val id: Long = 0,
    val storeName: String,
    val totalAmount: Double,
    val date: Date,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColor: Int? = null,
    val notes: String? = null,
    val imagePath: String? = null,
    val lineItems: List<LineItemModel> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Domain model representing a single item on a receipt
 */
data class LineItemModel(
    val id: Long = 0,
    val name: String,
    val quantity: Int = 1,
    val price: Double,
    val total: Double = price * quantity,
    val categoryId: Long? = null,
    val categoryName: String? = null
)