package com.example.struku.domain.model

/**
 * Domain model representing an expense or income category
 */
data class CategoryModel(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val iconName: String? = null,
    val isDefault: Boolean = false,
    val isExpense: Boolean = true
)