package com.example.struku.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.struku.data.model.Receipt
import com.example.struku.data.local.dao.CategoryDao
import com.example.struku.data.local.dao.LineItemDao
import com.example.struku.data.local.dao.ReceiptDao
import com.example.struku.data.model.Category
import com.example.struku.data.model.LineItem
import com.example.struku.data.local.database.Converters
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Receipt::class, LineItem::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receiptDao(): ReceiptDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "struku_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, useEncryption: Boolean = true): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )

                if (useEncryption) {
                    val passphrase = SQLiteDatabase.getBytes("struku_secure_key".toCharArray())
                    val factory = SupportFactory(passphrase)
                    builder.openHelperFactory(factory)
                }

                val instance = builder
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}