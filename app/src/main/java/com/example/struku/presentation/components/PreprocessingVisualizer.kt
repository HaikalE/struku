package com.example.struku.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap

@Composable
fun PreprocessingVisualizer(
    originalImage: Bitmap?,
    processedImage: Bitmap?,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Original",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (originalImage != null) {
                        Image(
                            bitmap = originalImage.asImageBitmap(),
                            contentDescription = "Original image",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No image")
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Processed",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (processedImage != null) {
                        Image(
                            bitmap = processedImage.asImageBitmap(),
                            contentDescription = "Processed image",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Processing...")
                        }
                    }
                }
            }
        }
    }
}