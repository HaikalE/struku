package com.example.struku.di

import android.content.Context
import com.example.struku.data.parser.ReceiptParser
import com.example.struku.data.recognition.AdvancedImagePreprocessor
import com.example.struku.data.recognition.MlKitOcrEngine
import com.example.struku.data.repository.CategoryRepositoryImpl
import com.example.struku.data.repository.ReceiptRepositoryImpl
import com.example.struku.domain.repository.CategoryRepository
import com.example.struku.domain.repository.ReceiptRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReceiptRepository(
        receiptRepositoryImpl: ReceiptRepositoryImpl
    ): ReceiptRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideMlKitOcrEngine(@ApplicationContext context: Context): MlKitOcrEngine {
            return MlKitOcrEngine(context)
        }

        @Provides
        @Singleton
        fun provideAdvancedImagePreprocessor(mlKitOcrEngine: MlKitOcrEngine): AdvancedImagePreprocessor {
            return AdvancedImagePreprocessor(mlKitOcrEngine)
        }

        @Provides
        @Singleton
        fun provideReceiptParser(advancedImagePreprocessor: AdvancedImagePreprocessor): ReceiptParser {
            return ReceiptParser(advancedImagePreprocessor)
        }
    }
}