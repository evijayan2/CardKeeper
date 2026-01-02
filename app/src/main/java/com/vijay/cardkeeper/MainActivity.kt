package com.vijay.cardkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.vijay.cardkeeper.ui.navigation.CardKeeperNavHost
import com.vijay.cardkeeper.ui.theme.CardKeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        System.out.println("CardKeeperUI: MainActivity onCreate called")

        if (com.vijay.cardkeeper.util.KeyManager.cachedPassphrase == null) {
            if (com.vijay.cardkeeper.util.KeyManager.isKeyCreated(this)) {
                System.out.println("CardKeeperUI: MainActivity: Redirection to AuthActivity")
                startActivity(android.content.Intent(this, AuthActivity::class.java))
                finish()
                return
            }
        }
        
        // Trigger background migration
        val app = application as CardKeeperApplication
        lifecycleScope.launch {
           app.container.imageMigrationManager.performMigration()
        }

        setContent {
            CardKeeperApp()
        }
    }
}

@Composable
private fun CardKeeperApp() {
    val context = LocalContext.current
    val userPreferencesRepository = (context.applicationContext as CardKeeperApplication).container.userPreferencesRepository
    val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = "SYSTEM")
    
    println("CardKeeperUI: CardKeeperApp Composing. Theme: $themeMode")

    val useDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    // Create NavController at this level so it survives theme changes
    val navController = rememberNavController()

    CardKeeperTheme(darkTheme = useDarkTheme) {
        CardKeeperNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}
