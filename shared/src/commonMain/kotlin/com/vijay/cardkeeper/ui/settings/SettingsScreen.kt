package com.vijay.cardkeeper.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dateFormat by viewModel.dateFormat.collectAsState()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val reminder1Days by viewModel.reminder1Days.collectAsState()
    val reminder2Days by viewModel.reminder2Days.collectAsState()
    val reminder3Days by viewModel.reminder3Days.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                Text("Theme", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    selected = themeMode == "SYSTEM",
                    onClick = { viewModel.selectTheme("SYSTEM") },
                    text = "System Default"
                )
                ThemeOption(
                    selected = themeMode == "LIGHT",
                    onClick = { viewModel.selectTheme("LIGHT") },
                    text = "Light"
                )
                ThemeOption(
                    selected = themeMode == "DARK",
                    onClick = { viewModel.selectTheme("DARK") },
                    text = "Dark"
                )
            }

            HorizontalDivider()

            // General Section
            SettingsSection(title = "General") {
                Text("Date Format", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                DateFormatOption(
                    selected = dateFormat == "DEFAULT",
                    onClick = { viewModel.selectDateFormat("DEFAULT") },
                    text = "App Decides (Country Spec.)"
                )
                DateFormatOption(
                    selected = dateFormat == "SYSTEM",
                    onClick = { viewModel.selectDateFormat("SYSTEM") },
                    text = "System Default"
                )
                DateFormatOption(
                    selected = dateFormat == "CUSTOM",
                    onClick = { viewModel.selectDateFormat("CUSTOM") },
                    text = "Custom (DD/MM/YYYY)" // Placeholder for now
                )
            }

            HorizontalDivider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Item Expiration",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Get notified when items are expiring",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
                
                if (notificationsEnabled) {
                     Spacer(modifier = Modifier.height(16.dp))
                     Text("Reminder Schedule (Days Before)", style = MaterialTheme.typography.titleSmall)
                     Spacer(modifier = Modifier.height(8.dp))
                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         OutlinedTextField(
                             value = reminder1Days.toString(),
                             onValueChange = { viewModel.updateReminder1Days(it) },
                             label = { Text("1st") },
                             modifier = Modifier.weight(1f),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                             singleLine = true
                         )
                         OutlinedTextField(
                             value = reminder2Days.toString(),
                             onValueChange = { viewModel.updateReminder2Days(it) },
                             label = { Text("2nd") },
                             modifier = Modifier.weight(1f),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                             singleLine = true
                         )
                         OutlinedTextField(
                             value = reminder3Days.toString(),
                             onValueChange = { viewModel.updateReminder3Days(it) },
                             label = { Text("3rd") },
                             modifier = Modifier.weight(1f),
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                             singleLine = true
                         )
                     }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // General Notifications (reuse same preference for now or stub)
               Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "General Updates",
                            style = MaterialTheme.typography.titleSmall
                        )
                         Text(
                            text = "Receive app updates and news",
                            style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = false, // Placeholder
                        onCheckedChange = { /* TODO */ },
                        enabled = false // Not implemented yet
                    )
                }
            }
            
            HorizontalDivider()

            // Data Management
            SettingsSection(title = "Data Management") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Import */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Data")
                    }
                    OutlinedButton(
                        onClick = { /* TODO: Export */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Data")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
fun ThemeOption(selected: Boolean, onClick: () -> Unit, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DateFormatOption(selected: Boolean, onClick: () -> Unit, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
