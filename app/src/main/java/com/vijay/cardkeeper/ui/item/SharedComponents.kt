package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(title: String) {
    Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun DetailRow(
        label: String,
        value: String?,
        isCopyable: Boolean = false,
        onCopy: (() -> Unit)? = null,
        fontFamily: androidx.compose.ui.text.font.FontFamily? = null
) {
    if (value.isNullOrEmpty()) return

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
        )
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = fontFamily,
                    modifier = Modifier.weight(1f)
            )
            if (isCopyable) {
                IconButton(
                        onClick = {
                            if (onCopy != null) {
                                onCopy()
                            } else {
                                val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as
                                                ClipboardManager
                                val clip = ClipData.newPlainText(label, value)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
