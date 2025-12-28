package com.vijay.cardkeeper.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.R

@Composable
fun WelcomeScreen(
    onRegisterClick: () -> Unit,
    onQuitClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val logoSize = screenHeight * 0.25f

            // App Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_app_logo_1),
                contentDescription = "App Logo",
                modifier = Modifier.size(logoSize)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Secure your personal data",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Register Button
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Register Biometrics")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quit Button
            OutlinedButton(
                onClick = onQuitClick,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Quit")
            }
        }
    }
}
