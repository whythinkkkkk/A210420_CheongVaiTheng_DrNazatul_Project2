package com.example.inventory

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarketplaceScreen(
    viewModel: OrderViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var nameInput by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = onNavigateToCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Shop Now")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── HEADER ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Hello,", fontSize = 16.sp, color = Color.Gray)
                    Text(
                        text = uiState.userName,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Cart icon with badge
                    BadgedBox(
                        badge = {
                            if (uiState.cartCount > 0) {
                                Badge { Text("${uiState.cartCount}") }
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { onNavigateToCart() },
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Settings icon
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { onNavigateToSettings() },
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ── NAME INPUT ───────────────────────────────────────────────────────
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Update your name", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Type your name here...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateUserName(nameInput)
                            nameInput = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Name")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── BANNER ───────────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RECYCLE & SAVE\nGet 10% Off Today!",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // ── PRODUCTS ─────────────────────────────────────────────────────────
            Text(
                text = "Recommended Products",
                modifier = Modifier.padding(start = 20.dp),
                fontWeight = FontWeight.Bold
            )

            MarketProductCard("Cabbage", 2.99, "From Cameron Highlands, 100% fresh") {
                viewModel.selectItem("Cabbage", 2.99)
            }
            MarketProductCard("Potato", 0.99, "Russet potatoes with fluffy texture") {
                viewModel.selectItem("Potato", 0.99)
            }
            MarketProductCard("Carrot", 1.50, "Fresh carrots, rich in vitamin A") {
                viewModel.selectItem("Carrot", 1.50)
            }
            MarketProductCard("Spinach", 1.80, "Organic spinach, locally grown") {
                viewModel.selectItem("Spinach", 1.80)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MarketProductCard(name: String, price: Double, detail: String, onSelect: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable {
                expanded = !expanded
                onSelect()
            }
            .animateContentSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold)
                    Text(text = "RM $price", color = MaterialTheme.colorScheme.primary)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = detail, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}