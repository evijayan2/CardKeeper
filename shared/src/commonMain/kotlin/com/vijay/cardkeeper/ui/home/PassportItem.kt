package com.vijay.cardkeeper.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.Passport

@Composable
fun PassportItem(passport: Passport, onClick: () -> Unit) {
    val platformContext = LocalPlatformContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Full Watermark (Country Flag)
            val countryCode = passport.countryCode?.lowercase() ?: "us"
            val backgroundUrl = when (countryCode) {
                "usa", "us" -> "https://flagcdn.com/w320/us.png"
                "ind", "in", "india" -> "https://flagcdn.com/w320/in.png"
                else -> if (countryCode.length == 2) "https://flagcdn.com/w320/$countryCode.png" else null
            }

            if (backgroundUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(backgroundUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.15f,
                    modifier = Modifier.matchParentSize()
                )
            }

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document Icon or Image
                if (passport.frontImagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(platformContext)
                            .data(passport.frontImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Passport - ${passport.countryCode}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${passport.givenNames} ${passport.surname}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "No: ${passport.passportNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!passport.dateOfExpiry.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Expires: ${passport.dateOfExpiry}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ExpirationBadge(expiryDateStr = passport.dateOfExpiry)
                        }
                    }
                }

                // Country Flag (Online via FlagCDN)
                val flagUrl = when (countryCode) {
                    "usa", "us" -> "https://flagcdn.com/w160/us.png"
                    "ind", "in", "india" -> "https://flagcdn.com/w160/in.png"
                    else -> if (countryCode.length == 2) "https://flagcdn.com/w160/$countryCode.png" else null
                }

                if (flagUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(platformContext)
                            .data(flagUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Country Flag",
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
