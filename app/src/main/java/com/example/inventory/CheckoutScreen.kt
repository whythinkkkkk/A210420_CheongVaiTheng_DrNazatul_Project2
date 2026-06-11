package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CheckoutScreen(
    viewModel: OrderViewModel,
    onGoToCart: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var quantityInput by remember { mutableStateOf(1f) }
    var addedSnackbar by remember { mutableStateOf(false) }
    var lastAddedItem by remember { mutableStateOf("") }

    val ecoProducts = listOf(
        Triple("Mineral Water", 1.35, "Chilled 600ml mineral water"),
        Triple("Cabbage", 2.99, "From Cameron Highlands, 100% fresh"),
        Triple("Potato", 0.99, "Russet potatoes with fluffy texture"),
        Triple("Carrot", 1.50, "Fresh carrots, rich in vitamin A"),
        Triple("Spinach", 1.80, "Organic spinach, locally grown")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── HEADER ───────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Choose Products",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // Cart badge button
                BadgedBox(
                    badge = {
                        if (uiState.cartCount > 0) {
                            Badge { Text("${uiState.cartCount}") }
                        }
                    }
                ) {
                    TextButton(onClick = onGoToCart) {
                        Text("View Cart")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SNACKBAR FEEDBACK ────────────────────────────────────────────────
            if (addedSnackbar) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "✓ $lastAddedItem added to cart!",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── PRODUCT LIST ─────────────────────────────────────────────────────
            ecoProducts.forEach { (name, price, _) ->
                val isSelected = uiState.itemName == name

                OutlinedCard(
                    onClick = { viewModel.selectItem(name, price) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectItem(name, price) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                text = "RM ${String.format("%.2f", price)} per unit",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.itemName.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Quantity", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${quantityInput.roundToInt()} unit(s)",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            valueRange = 1f..15f,
                            steps = 13,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        val subtotal = uiState.itemPrice * quantityInput.roundToInt()
                        Text(
                            text = "Subtotal: RM ${String.format("%.2f", subtotal)}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        } // end scrollable column

        // ── STICKY BOTTOM ACTIONS ─────────────────────────────────────────────
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Button(
                    onClick = {
                        val qty = quantityInput.roundToInt()
                        viewModel.addToCart(uiState.itemName, uiState.itemPrice, qty)
                        lastAddedItem = uiState.itemName
                        addedSnackbar = true
                        quantityInput = 1f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.itemName.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add to Cart")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onGoToCart,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.cartCount > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Cart (${uiState.cartCount} items)")
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}