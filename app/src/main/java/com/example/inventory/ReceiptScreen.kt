package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState

@Composable
fun ReceiptScreen(
    viewModel: OrderViewModel,
    onReturnHome: () -> Unit
) {
    // We use the most recent orders from history to display what was just purchased.
    // completeOrder() clears the cart, so we snapshot before navigating here.
    // The ViewModel holds the receipt snapshot in receiptSnapshot.
    val receiptItems = viewModel.receiptSnapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Payment Successful!",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Thank you for your purchase",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "RECEIPT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (receiptItems.isNotEmpty()) {
                    receiptItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.name} × ${item.quantity}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "RM ${String.format("%.2f", item.totalPrice)}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Text(
                        "Order recorded.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Paid:", fontWeight = FontWeight.Bold)
                    Text(
                        "RM ${String.format("%.2f", receiptItems.sumOf { it.totalPrice })}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Paid via ${viewModel.receiptPaymentMethod}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReturnHome,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Marketplace", fontWeight = FontWeight.Bold)
        }
    }
}