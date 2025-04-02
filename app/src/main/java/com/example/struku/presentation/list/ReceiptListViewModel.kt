package com.example.struku.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.struku.domain.model.ReceiptWithItems
import com.example.struku.domain.usecase.GetReceiptsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val getReceiptsUseCase: GetReceiptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceiptListUiState>(ReceiptListUiState.Loading)
    val uiState: StateFlow<ReceiptListUiState> = _uiState

    private val _filterPeriod = MutableStateFlow(FilterPeriod.MONTH)
    val filterPeriod: StateFlow<FilterPeriod> = _filterPeriod

    private val _filterStartDate = MutableStateFlow(firstDayOfMonth())
    val filterStartDate: StateFlow<Date> = _filterStartDate

    private val _filterEndDate = MutableStateFlow(lastDayOfMonth())
    val filterEndDate: StateFlow<Date> = _filterEndDate

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    init {
        loadReceipts()
    }

    fun loadReceipts() {
        viewModelScope.launch {
            _uiState.value = ReceiptListUiState.Loading

            when (_filterPeriod.value) {
                FilterPeriod.ALL -> {
                    getReceiptsUseCase.getAllReceipts()
                        .catch { e ->
                            _uiState.value = ReceiptListUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { receipts ->
                            _uiState.value = ReceiptListUiState.Success(receipts)
                            calculateTotal(receipts)
                        }
                }
                FilterPeriod.MONTH -> {
                    getReceiptsUseCase.getCurrentMonthReceipts()
                        .catch { e ->
                            _uiState.value = ReceiptListUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { receipts ->
                            _uiState.value = ReceiptListUiState.Success(receipts)
                            calculateTotal(receipts)
                        }
                }
                FilterPeriod.CUSTOM -> {
                    getReceiptsUseCase.getReceiptsByDateRange(_filterStartDate.value, _filterEndDate.value)
                        .catch { e ->
                            _uiState.value = ReceiptListUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { receipts ->
                            _uiState.value = ReceiptListUiState.Success(receipts)
                            calculateTotal(receipts)
                        }
                }
            }
        }
    }

    fun setFilterPeriod(period: FilterPeriod) {
        _filterPeriod.value = period

        // Update date range based on period
        when (period) {
            FilterPeriod.MONTH -> {
                _filterStartDate.value = firstDayOfMonth()
                _filterEndDate.value = lastDayOfMonth()
            }
            FilterPeriod.ALL -> {
                // Keep dates as they are
            }
            FilterPeriod.CUSTOM -> {
                // Keep dates as they are
            }
        }

        loadReceipts()
    }

    fun setCustomDateRange(startDate: Date, endDate: Date) {
        _filterStartDate.value = startDate
        _filterEndDate.value = endDate
        _filterPeriod.value = FilterPeriod.CUSTOM
        loadReceipts()
    }

    private fun calculateTotal(receipts: List<ReceiptWithItems>) {
        _totalAmount.value = receipts.sumOf { it.totalAmount }
    }

    private fun firstDayOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun lastDayOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}

sealed class ReceiptListUiState {
    object Loading : ReceiptListUiState()
    data class Success(val receipts: List<ReceiptWithItems>) : ReceiptListUiState()
    data class Error(val message: String) : ReceiptListUiState()
}

enum class FilterPeriod {
    ALL, MONTH, CUSTOM
}