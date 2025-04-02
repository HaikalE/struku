package com.example.struku.data.repository

import com.example.struku.data.local.dao.CategoryDao
import com.example.struku.data.model.Category
import com.example.struku.domain.model.CategoryModel
import com.example.struku.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.graphics.Color

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryModel>> {
        return categoryDao.getAllCategories().map { categories ->
            categories.map { it.toDomainModel() }
        }
    }

    override fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryModel>> {
        return categoryDao.getCategoriesByType(isExpense).map { categories ->
            categories.map { it.toDomainModel() }
        }
    }

    override suspend fun getCategoryById(categoryId: Long): CategoryModel? {
        return categoryDao.getCategoryById(categoryId)?.toDomainModel()
    }

    override suspend fun getDefaultCategory(isExpense: Boolean): CategoryModel {
        var defaultCategory = categoryDao.getDefaultCategory(isExpense)

        // Create default category if none exists
        if (defaultCategory == null) {
            val newDefaultCategory = if (isExpense) {
                Category(
                    name = "Lainnya",
                    color = Color.GRAY,
                    isDefault = true,
                    isExpense = true
                )
            } else {
                Category(
                    name = "Pendapatan",
                    color = Color.GREEN,
                    isDefault = true,
                    isExpense = false
                )
            }

            val id = categoryDao.insert(newDefaultCategory)
            defaultCategory = categoryDao.getCategoryById(id)
        }

        return defaultCategory!!.toDomainModel()
    }

    override suspend fun saveCategory(category: CategoryModel): Long {
        val entity = Category(
            id = category.id,
            name = category.name,
            color = category.color,
            iconName = category.iconName,
            isDefault = category.isDefault,
            isExpense = category.isExpense
        )
        return categoryDao.insert(entity)
    }

    override suspend fun deleteCategory(categoryId: Long) {
        val category = categoryDao.getCategoryById(categoryId) ?: return

        // Don't delete default categories
        if (category.isDefault) return

        categoryDao.delete(category)
    }

    override suspend fun initializeDefaultCategories() {
        // Only initialize if no categories exist
        if (categoryDao.getCategoryCount() > 0) return

        val defaultCategories = listOf(
            // Expense categories
            Category(
                name = "Makanan & Minuman",
                color = Color.parseColor("#FF9800"),
                isDefault = false,
                isExpense = true,
                iconName = "restaurant"
            ),
            Category(
                name = "Belanja",
                color = Color.parseColor("#2196F3"),
                isDefault = false,
                isExpense = true,
                iconName = "shopping_cart"
            ),
            Category(
                name = "Transportasi",
                color = Color.parseColor("#4CAF50"),
                isDefault = false,
                isExpense = true,
                iconName = "directions_car"
            ),
            Category(
                name = "Lainnya",
                color = Color.GRAY,
                isDefault = true,
                isExpense = true,
                iconName = "more_horiz"
            ),

            // Income categories
            Category(
                name = "Gaji",
                color = Color.parseColor("#4CAF50"),
                isDefault = false,
                isExpense = false,
                iconName = "payments"
            ),
            Category(
                name = "Pendapatan",
                color = Color.GREEN,
                isDefault = true,
                isExpense = false,
                iconName = "attach_money"
            )
        )

        categoryDao.insertAll(defaultCategories)
    }

    // Extension function to convert entity to domain model
    private fun Category.toDomainModel(): CategoryModel {
        return CategoryModel(
            id = this.id,
            name = this.name,
            color = this.color,
            iconName = this.iconName,
            isDefault = this.isDefault,
            isExpense = this.isExpense
        )
    }
}