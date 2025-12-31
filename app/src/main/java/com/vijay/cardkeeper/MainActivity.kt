package com.vijay.cardkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vijay.cardkeeper.ui.theme.CardKeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userPreferencesRepository = (application as CardKeeperApplication).container.userPreferencesRepository
        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = "SYSTEM")

            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            // Notification Permission Request for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permission = android.Manifest.permission.POST_NOTIFICATIONS
                val context = androidx.compose.ui.platform.LocalContext.current
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle outcome if needed, e.g. update state
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        launcher.launch(permission)
                    }
                }
            }

            CardKeeperTheme(darkTheme = darkTheme) {
                // A surface container using the 'background' color from the theme
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    val navController = androidx.navigation.compose.rememberNavController()
                    com.vijay.cardkeeper.ui.navigation.CardKeeperNavHost(navController = navController)
                }
            }
        }
    }
}
