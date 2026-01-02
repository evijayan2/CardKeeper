package com.vijay.cardkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vijay.cardkeeper.ui.home.HomeScreen
import com.vijay.cardkeeper.ui.item.AddItemRoute
import com.vijay.cardkeeper.ui.item.ViewIdentityRoute
import com.vijay.cardkeeper.ui.item.ViewItemRoute
import com.vijay.cardkeeper.ui.search.SearchRoute

object CardKeeperDestinations {
    const val HOME_ROUTE = "home?tab={tab}"
    const val ADD_ITEM_ROUTE =
            "add_item?category={category}&itemId={itemId}&initialType={initialType}" // Support
    // params
    const val VIEW_ITEM_ROUTE = "view_item/{accountId}"
    const val VIEW_IDENTITY_ROUTE = "view_identity/{documentId}"
    const val VIEW_PASSPORT_ROUTE = "view_passport/{passportId}"
    const val VIEW_GREEN_CARD_ROUTE = "view_greencard/{gcId}"
    const val VIEW_AADHAR_ROUTE = "view_aadhar/{aadharId}"
    const val VIEW_PAN_CARD_ROUTE = "view_pancard/{panCardId}"
    const val VIEW_GIFT_CARD_ROUTE = "view_gift_card/{giftCardId}"
    const val VIEW_REWARDS_ROUTE = "view_rewards/{accountId}"
    const val SEARCH_ROUTE = "search"
    const val SETTINGS_ROUTE = "settings"
}

@Composable
fun CardKeeperNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    println("CardKeeperUI: CardKeeperNavHost RECOMPOSING")
    SideEffect {
        println("CardKeeperUI: CardKeeperNavHost SideEffect - current destination: ${navController.currentDestination?.route}")
    }
    NavHost(
            navController = navController,
            startDestination = "home",
            modifier = modifier
    ) {
        composable(
            route = CardKeeperDestinations.HOME_ROUTE,
            arguments = listOf(navArgument("tab") { 
                type = NavType.IntType
                defaultValue = -1  // -1 means no tab change
            })
        ) { backStackEntry ->
            val targetTab = backStackEntry.arguments?.getInt("tab") ?: -1
            val homeViewModel: com.vijay.cardkeeper.ui.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider.Factory)
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // Use key() to force stable composition and prevent rendering glitches
            androidx.compose.runtime.key("home_screen_stable") {
                com.vijay.cardkeeper.ui.home.HomeScreen(
                        navigateToItemEntry = { category, type ->
                            navController.navigate(
                                    "add_item?category=$category&initialType=${type ?: ""}"
                            )
                        },
                        viewModel = homeViewModel,
                    initialTab = if (targetTab >= 0) targetTab else null,
                        onCopyContent = { text ->
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Copied Text", text)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Copied", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        navigateToItemView = { itemId -> navController.navigate("view_item/$itemId") },
                        navigateToIdentityView = { docId ->
                            navController.navigate("view_identity/$docId")
                        },
                        navigateToPassportView = { passportId ->
                            navController.navigate("view_passport/$passportId")
                        },
                        navigateToGreenCardView = { gcId ->
                            navController.navigate("view_greencard/$gcId")
                        },
                        navigateToAadharView = { aadharId ->
                            navController.navigate("view_aadhar/$aadharId")
                        },
                        navigateToPanCardView = { panCardId ->
                            navController.navigate("view_pancard/$panCardId")
                        },
                        navigateToGiftCardView = { giftCardId ->
                            navController.navigate("view_gift_card/$giftCardId")
                        },
                        navigateToRewardsView = { accountId ->
                            navController.navigate("view_rewards/$accountId")
                        },
                        navigateToSearch = {
                            navController.navigate(CardKeeperDestinations.SEARCH_ROUTE)
                        },
                        navigateToSettings = {
                            navController.navigate(CardKeeperDestinations.SETTINGS_ROUTE)
                        }
                )
            }
        }
        composable(route = CardKeeperDestinations.SEARCH_ROUTE) {
            val searchViewModel: com.vijay.cardkeeper.ui.viewmodel.SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider.Factory)
            com.vijay.cardkeeper.ui.search.SearchRoute(
                    onNavigateBack = { 
                    println("CardKeeperUI: AddItemRoute calling popBackStack")
                    navController.popBackStack()
                    println("CardKeeperUI: popBackStack completed")
                },
                    onResultClick = { id, type ->
                        val route =
                                when (type) {
                                    "Finance" -> "view_item/$id"
                                    "Identity" -> "view_identity/$id"
                                    "Passport" -> "view_passport/$id"
                                    "Green Card" -> "view_greencard/$id"
                                    "Aadhar" -> "view_aadhar/$id"
                                    "PAN" -> "view_pancard/$id"
                                    "PAN Card" -> "view_pancard/$id"
                                    "Gift Card" -> "view_gift_card/$id"

                                    "Rewards" -> "view_rewards/$id"
                                    else -> null
                                }
                        route?.let { navController.navigate(it) }
                    },
                    viewModel = searchViewModel
            )
        }
        composable(route = CardKeeperDestinations.SETTINGS_ROUTE) {
            val settingsViewModel: com.vijay.cardkeeper.ui.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider.Factory)
            com.vijay.cardkeeper.ui.settings.SettingsRoute(
                navigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }
        composable(
                route = CardKeeperDestinations.ADD_ITEM_ROUTE,
                arguments =
                        listOf(
                                navArgument("category") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                },
                                navArgument("itemId") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                },
                                navArgument("initialType") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                    nullable = true
                                }
                        )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getInt("category") ?: 0
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val initialType = backStackEntry.arguments?.getString("initialType")

            AddItemRoute(
                    navigateBack = { savedCategory ->
                        // Map category to tab: 0=Finance, 1=Identity, 2=Passport, 3=Rewards
                        val targetTab = when (savedCategory) {
                            0 -> 0  // Finance → Finance tab
                            1 -> 1  // Identity → Identity tab
                            2 -> 2  // Passport → Passports tab
                            3 -> 3  // Rewards Card → Rewards tab
                            4 -> 1  // Green Card → Identity tab
                            5 -> 1  // Aadhar → Identity tab
                            6 -> 3  // Gift Card → Rewards tab
                            7 -> 1  // PAN Card → Identity tab
                            else -> 0
                        }
                        navController.navigate("home?tab=$targetTab") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    initialCategory = category,
                    documentId = if (itemId > 0) itemId else null,
                    documentType = initialType
            )
        }

        composable(
                route = CardKeeperDestinations.VIEW_IDENTITY_ROUTE,
                arguments = listOf(navArgument("documentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getInt("documentId") ?: 0
            ViewIdentityRoute(
                    documentId = documentId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=1&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_ITEM_ROUTE,
                arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("accountId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewItemRoute(
                    itemId = itemId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=0&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_PASSPORT_ROUTE,
                arguments = listOf(navArgument("passportId") { type = NavType.IntType })
        ) { backStackEntry ->
            val passportId = backStackEntry.arguments?.getInt("passportId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewPassportRoute(
                    passportId = passportId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=2&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_GREEN_CARD_ROUTE,
                arguments = listOf(navArgument("gcId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gcId = backStackEntry.arguments?.getInt("gcId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewGreenCardRoute(
                    greenCardId = gcId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=4&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_AADHAR_ROUTE,
                arguments = listOf(navArgument("aadharId") { type = NavType.IntType })
        ) { backStackEntry ->
            val aadharId = backStackEntry.arguments?.getInt("aadharId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewAadharCardRoute(
                    aadharCardId = aadharId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=5&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_PAN_CARD_ROUTE,
                arguments = listOf(navArgument("panCardId") { type = NavType.IntType })
        ) { backStackEntry ->
            val panCardId = backStackEntry.arguments?.getInt("panCardId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewPanCardRoute(
                    panCardId = panCardId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=7&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_GIFT_CARD_ROUTE,
                arguments = listOf(navArgument("giftCardId") { type = NavType.IntType })
        ) { backStackEntry ->
            val giftCardId = backStackEntry.arguments?.getInt("giftCardId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewGiftCardRoute(
                    giftCardId = giftCardId,
                    navigateBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("add_item?category=6&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_REWARDS_ROUTE,
                arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewRewardsRoute(
                    itemId = accountId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=3&itemId=$id") }
            )
        }
    }
}
