package com.example.struku.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.struku.domain.model.LineItemModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewScreen(
    navigateUp: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: ReceiptReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Receipt") },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveReceipt()
                    navigateToHome()
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Receipt")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Store: ${state.storeName}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = state.storeName,
                    onValueChange = { viewModel.updateStoreName(it) },
                    label = { Text("Store Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(state.lineItems) { item ->
                LineItemCard(
                    item = item,
                    onItemUpdate = { updatedItem ->
                        viewModel.updateItemDescription(updatedItem)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB
            }
        }
    }
}

@Composable
fun LineItemCard(
    item: LineItemModel,
    onItemUpdate: (LineItemModel) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf(item.name) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editMode) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = {
                            editMode = false
                            onItemUpdate(item.copy(name = itemName))
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Changes")
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${item.quantity} x ${currencyFormat.format(item.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = { editMode = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Item")
                    }
                    
                    Text(
                        text = currencyFormat.format(item.total),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
