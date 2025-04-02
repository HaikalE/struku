package com.example.struku.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.struku.data.local.database.Converters
import java.util.Date

@Entity(tableName = "receipts")
@TypeConverters(Converters::class)
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeName: String,
    val totalAmount: Double,
    val date: Date,
    val categoryId: Long? = null,
    val notes: String? = null,
    val imagePath: String? = null,
    val rawOcrText: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)