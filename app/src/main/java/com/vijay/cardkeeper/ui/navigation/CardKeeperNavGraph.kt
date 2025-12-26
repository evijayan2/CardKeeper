package com.vijay.cardkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vijay.cardkeeper.ui.home.HomeScreen
import com.vijay.cardkeeper.ui.item.AddItemScreen
import com.vijay.cardkeeper.ui.item.ViewIdentityScreen
import com.vijay.cardkeeper.ui.item.ViewItemScreen

object CardKeeperDestinations {
    const val HOME_ROUTE = "home"
    const val ADD_ITEM_ROUTE =
            "add_item?category={category}&itemId={itemId}&initialType={initialType}" // Support
    // params
    const val VIEW_ITEM_ROUTE = "view_item/{accountId}"
    const val VIEW_IDENTITY_ROUTE = "view_identity/{documentId}"
    const val VIEW_PASSPORT_ROUTE = "view_passport/{passportId}"
    const val VIEW_REWARDS_ROUTE = "view_rewards/{accountId}"
}

@Composable
fun CardKeeperNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
            navController = navController,
            startDestination = CardKeeperDestinations.HOME_ROUTE,
            modifier = modifier
    ) {
        composable(route = CardKeeperDestinations.HOME_ROUTE) {
            HomeScreen(
                    navigateToItemEntry = { category, type ->
                        navController.navigate(
                                "add_item?category=$category&initialType=${type ?: ""}"
                        )
                    },
                    navigateToItemView = { itemId -> navController.navigate("view_item/$itemId") },
                    navigateToIdentityView = { docId ->
                        navController.navigate("view_identity/$docId")
                    },
                    navigateToPassportView = { passportId ->
                        navController.navigate("view_passport/$passportId")
                    },
                    navigateToRewardsView = { accountId ->
                        navController.navigate("view_rewards/$accountId")
                    }
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

            AddItemScreen(
                    navigateBack = { navController.popBackStack() },
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
            ViewIdentityScreen(
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
            com.vijay.cardkeeper.ui.item.ViewItemScreen(
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
            com.vijay.cardkeeper.ui.item.ViewPassportScreen(
                    passportId = passportId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=2&itemId=$id") }
            )
        }
        composable(
                route = CardKeeperDestinations.VIEW_REWARDS_ROUTE,
                arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: 0
            com.vijay.cardkeeper.ui.item.ViewRewardsScreen(
                    itemId = accountId,
                    navigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("add_item?category=3&itemId=$id") }
            )
        }
    }
}
