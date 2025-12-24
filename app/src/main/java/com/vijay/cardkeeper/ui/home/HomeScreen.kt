package com.vijay.cardkeeper.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        modifier: Modifier = Modifier,
        navigateToItemEntry: () -> Unit,
        navigateToItemView: (Int) -> Unit, // New callback
        viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val financialList by viewModel.financialAccounts.collectAsState()
    val identityList by viewModel.identityDocuments.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Finance", "Identity")

    Scaffold(
            topBar = { TopAppBar(title = { Text("Card Keeper") }) },
            floatingActionButton = {
                FloatingActionButton(onClick = navigateToItemEntry) {
                    Icon(Icons.Filled.Add, "Add Item")
                }
            }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                        if (index == 0) Icons.Default.Home else Icons.Default.Face,
                                        contentDescription = null
                                )
                            }
                    )
                }
            }

            when (selectedTab) {
                0 -> FinancialList(financialList, navigateToItemView)
                1 -> IdentityList(identityList)
            }
        }
    }
}

@Composable
fun FinancialList(list: List<FinancialAccount>, onItemClick: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(list) { account ->
            Card(
                    modifier =
                            Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                                onItemClick(account.id)
                            },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = account.institutionName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        // Type Tag
                        SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                            account.type.name.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                enabled = false // Read-only tag
                        )
                    }

                    Text(text = account.accountName, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = "•••• ${account.number.takeLast(4)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun IdentityList(list: List<IdentityDocument>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(list) { doc ->
            Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                            text = doc.type.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(text = doc.country, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = doc.docNumber, style = MaterialTheme.typography.bodyLarge)
                    if (doc.expiryDate != null) {
                        // TODO: Format Date
                        Text(
                                text = "Exp: ${doc.expiryDate}",
                                color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
