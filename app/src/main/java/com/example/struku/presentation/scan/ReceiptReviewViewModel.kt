package com.example.struku.presentation.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.struku.domain.model.LineItemModel
import com.example.struku.domain.model.ReceiptWithItems
import com.example.struku.domain.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ReceiptReviewState(
    val receiptId: Long? = null,
    val storeName: String = "",
    val totalAmount: Double = 0.0,
    val date: Date = Date(),
    val lineItems: List<LineItemModel> = emptyList(),
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val notes: String? = null,
    val imagePath: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ReceiptReviewState())
    val state: StateFlow<ReceiptReviewState> = _state.asStateFlow()

    init {
        // Get receipt ID from navigation arguments
        savedStateHandle.get<Long>("receiptId")?.let { receiptId ->
            loadReceipt(receiptId)
        }
    }

    private fun loadReceipt(receiptId: Long) {
        viewModelScope.launch {
            receiptRepository.getReceiptWithItems(receiptId)?.let { receipt ->
                _state.value = _state.value.copy(
                    receiptId = receipt.id,
                    storeName = receipt.storeName,
                    totalAmount = receipt.totalAmount,
                    date = receipt.date,
                    lineItems = receipt.lineItems,
                    categoryId = receipt.categoryId,
                    categoryName = receipt.categoryName,
                    notes = receipt.notes,
                    imagePath = receipt.imagePath
                )
            }
        }
    }

    fun updateStoreName(name: String) {
        _state.value = _state.value.copy(storeName = name)
    }

    fun updateItemDescription(updatedItem: LineItemModel) {
        val updatedItems = _state.value.lineItems.map { item ->
            if (item.id == updatedItem.id) updatedItem else item
        }
        _state.value = _state.value.copy(lineItems = updatedItems)
        
        // Recalculate total
        val newTotal = updatedItems.sumOf { it.total }
        _state.value = _state.value.copy(totalAmount = newTotal)
    }

    fun saveReceipt() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            
            val receipt = ReceiptWithItems(
                id = _state.value.receiptId ?: 0,
                storeName = _state.value.storeName,
                totalAmount = _state.value.totalAmount,
                date = _state.value.date,
                lineItems = _state.value.lineItems,
                categoryId = _state.value.categoryId,
                categoryName = _state.value.categoryName,
                categoryColor = null, // Add if needed
                notes = _state.value.notes,
                imagePath = _state.value.imagePath,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            receiptRepository.saveReceiptWithItems(receipt)
            _state.value = _state.value.copy(isSaving = false)
        }
    }
}