package com.example.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderHistoryScreen(
    viewModel: OrderViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    // Collect the dynamic database stream
    val historyItems by viewModel.orderHistory.collectAsState()

    // Group items by billId
    val groupedOrders = historyItems.groupBy { it.billId }
        .map { (billId, items) ->
            BillGroup(
                billId = billId,
                items = items,
                status = items.firstOrNull()?.status ?: "Pending",
                date = items.firstOrNull()?.date ?: "",
                totalAmount = items.sumOf { it.totalPrice }
            )
        }
        .sortedByDescending { it.date }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("< Back") }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Order History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            if (groupedOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders yet!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(groupedOrders) { bill ->
                        BillCard(
                            bill = bill,
                            onClick = {
                                viewModel.selectBillForDetail(bill.items)
                                onNavigateToDetail()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class BillGroup(
    val billId: String,
    val items: List<OrderEntity>,
    val status: String,
    val date: String,
    val totalAmount: Double
)

@Composable
fun BillCard(
    bill: BillGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bill ID: ${bill.billId.take(8)}...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(text = bill.date, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${bill.items.size} item(s)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = bill.items.take(2).joinToString(", ") { it.name } +
                        if (bill.items.size > 2) " + ${bill.items.size - 2} more" else "",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RM ${String.format("%.2f", bill.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )

                // Status Badge
                val statusColor = if (bill.status == "Received") {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
                Text(
                    text = bill.status,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = statusColor
                )
            }
        }
    }
}