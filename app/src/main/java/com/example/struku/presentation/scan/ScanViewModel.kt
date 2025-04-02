package com.example.struku.presentation.scan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.struku..domain.model.LineItemModel
import com.example.struku..domain.model.ReceiptWithItems
import com.example.struku..domain.repository.CategoryRepository
import com.example.struku..domain.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receiptRepository: ReceiptRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private val _extractedData = MutableStateFlow<ReceiptData?>(null)
    val extractedData: StateFlow<ReceiptData?> = _extractedData

    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Process image and extract text using ML Kit
    fun processImage(bitmap: Bitmap) {
        _scanState.value = ScanState.Processing

        val image = InputImage.fromBitmap(bitmap, 0)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text

                // Save image to internal storage
                val savedImagePath = saveImageToInternalStorage(bitmap)

                // Parse text to extract receipt data
                val receiptData = parseReceiptText(rawText, savedImagePath)
                _extractedData.value = receiptData

                _scanState.value = ScanState.Success(rawText)
            }
            .addOnFailureListener { e ->
                _scanState.value = ScanState.Error("Text recognition failed: ${e.message}")
            }
    }

    // Process image from Uri
    fun processImageFromUri(uri: Uri) {
        try {
            _scanState.value = ScanState.Processing

            val image = InputImage.fromFilePath(context, uri)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text

                    // Save a copy of the image to internal storage
                    val bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    val savedImagePath = saveImageToInternalStorage(bitmap)

                    // Parse text to extract receipt data
                    val receiptData = parseReceiptText(rawText, savedImagePath)
                    _extractedData.value = receiptData

                    _scanState.value = ScanState.Success(rawText)
                }
                .addOnFailureListener { e ->
                    _scanState.value = ScanState.Error("Text recognition failed: ${e.message}")
                }
        } catch (e: Exception) {
            _scanState.value = ScanState.Error("Failed to process image: ${e.message}")
        }
    }

    // Save receipt data to database
    fun saveReceipt(data: ReceiptData) {
        viewModelScope.launch {
            try {
                // Get default category
                val defaultCategory = categoryRepository.getDefaultCategory(true)

                // Convert extracted data to domain model
                val lineItems = data.items.map { item ->
                    LineItemModel(
                        name = item.name,
                        quantity = item.quantity,
                        price = item.price,
                        total = item.price * item.quantity
                    )
                }

                val receipt = ReceiptWithItems(
                    storeName = data.merchantName ?: "Unknown Store",
                    totalAmount = data.total ?: 0.0,
                    date = data.date ?: Date(),
                    categoryId = defaultCategory.id,
                    categoryName = defaultCategory.name,
                    categoryColor = defaultCategory.color,
                    notes = data.notes,
                    imagePath = data.imagePath,
                    lineItems = lineItems
                )

                // Save to repository
                val savedId = receiptRepository.saveReceiptWithItems(receipt)
                _scanState.value = ScanState.Saved(savedId)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to save receipt: ${e.message}")
            }
        }
    }

    // Parse extracted text to get structured data
    private fun parseReceiptText(text: String, imagePath: String?): ReceiptData {
        // This is a simplified parser. In a real app, you'd use more sophisticated
        // text processing and pattern matching algorithms.
        val lines = text.split("\n")

        // Try to extract merchant name (usually in the first few lines)
        val merchantName = lines.firstOrNull { it.length > 3 }?.trim()

        // Look for date patterns
        val dateRegex = """(\d{1,2})[/.-](\d{1,2})[/.-](\d{2,4})""".toRegex()
        val dateMatch = dateRegex.find(text)
        val date = if (dateMatch != null) {
            try {
                // Simple date parsing - would need more robust handling in production
                val (day, month, year) = dateMatch.destructured
                val calendar = java.util.Calendar.getInstance()
                calendar.set(
                    year.toInt().let { if (it < 100) it + 2000 else it },
                    month.toInt() - 1,
                    day.toInt()
                )
                calendar.time
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

        // Extract potential line items and total
        val items = mutableListOf<LineItem>()
        var total: Double? = null

        // Look for total amount
        val totalRegex = """(?i)total[\s:]*(?:Rp|IDR)?[^\d]?(\d+[\.,]?\d*)""".toRegex()
        val totalMatch = totalRegex.find(text)
        if (totalMatch != null) {
            total = totalMatch.groupValues[1].replace(",", ".").toDoubleOrNull()
        }

        // Extract possible line items (this is very simplified)
        // In a real app, you would use more advanced NLP techniques
        for (i in 0 until lines.size) {
            val line = lines[i]

            // Look for price patterns at the end of lines
            val priceRegex = """(.+?)(?:Rp|IDR)?[^\d]?(\d+[\.,]?\d*)$""".toRegex()
            val match = priceRegex.find(line)

            if (match != null) {
                val name = match.groupValues[1].trim()
                val price = match.groupValues[2].replace(",", ".").toDoubleOrNull() ?: 0.0

                // Only add if the name is not too short and the price is reasonable
                if (name.length > 2 && price > 0 && price < (total ?: 10000000.0)) {
                    items.add(LineItem(name = name, price = price, quantity = 1))
                }
            }
        }

        return ReceiptData(
            merchantName = merchantName,
            date = date,
            total = total,
            items = items,
            notes = "",
            imagePath = imagePath,
            rawText = text
        )
    }

    // Save image to internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val fileName = "receipt_${UUID.randomUUID()}.jpg"
        val directory = File(context.filesDir, "receipt_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        return file.absolutePath
    }

    // Clear current data
    fun clearData() {
        _scanState.value = ScanState.Idle
        _extractedData.value = null
    }
}

// State for the scanning process
sealed class ScanState {
    object Idle : ScanState()
    object Processing : ScanState()
    data class Success(val text: String) : ScanState()
    data class Error(val message: String) : ScanState()
    data class Saved(val receiptId: Long) : ScanState()
}

// Data extracted from receipt
data class ReceiptData(
    val merchantName: String? = null,
    val date: Date? = null,
    val total: Double? = null,
    val items: List<LineItem> = emptyList(),
    val notes: String? = null,
    val imagePath: String? = null,
    val rawText: String? = null
)

// Extracted line item
data class LineItem(
    val name: String,
    val price: Double,
    val quantity: Int = 1
)