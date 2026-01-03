package com.vijay.cardkeeper.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardKeeperScanButtons(
    hasFrontImage: Boolean,
    onScanFront: () -> Unit,
    frontLabel: String = "Scan Front",
    frontCapturedLabel: String = "Front Captured",
    hasBackImage: Boolean = false,
    onScanBack: (() -> Unit)? = null,
    backLabel: String = "Scan Back",
    backCapturedLabel: String = "Back Captured",
    showBack: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Front Scan Button
        Button(
            onClick = onScanFront,
            modifier = Modifier.weight(1f),
            colors = if (hasFrontImage)
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            else ButtonDefaults.buttonColors()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PhotoCamera, "Front")
                Text(if (hasFrontImage) frontCapturedLabel else frontLabel)
            }
        }

        // Back Scan Button
        if (showBack && onScanBack != null) {
            Button(
                onClick = onScanBack,
                modifier = Modifier.weight(1f),
                colors = if (hasBackImage)
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PhotoCamera, "Back")
                    Text(if (hasBackImage) backCapturedLabel else backLabel)
                }
            }
        }
    }
}
