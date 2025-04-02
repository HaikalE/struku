package com.example.struku.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val iconName: String? = null,
    val isDefault: Boolean = false,
    val isExpense: Boolean = true
)