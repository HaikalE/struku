package com.example.struku.domain.repository


import com.example.struku.domain.model.CategoryModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing expense/income categories
 */
interface CategoryRepository {
    /**
     * Get all available categories
     */
    fun getAllCategories(): Flow<List<CategoryModel>>

    /**
     * Get categories filtered by expense or income type
     */
    fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryModel>>

    /**
     * Get a specific category by ID
     */
    suspend fun getCategoryById(categoryId: Long): CategoryModel?

    /**
     * Get or create a default category
     */
    suspend fun getDefaultCategory(isExpense: Boolean): CategoryModel

    /**
     * Save a category
     */
    suspend fun saveCategory(category: CategoryModel): Long

    /**
     * Delete a category
     */
    suspend fun deleteCategory(categoryId: Long)

    /**
     * Initialize default categories if none exist
     */
    suspend fun initializeDefaultCategories()
}