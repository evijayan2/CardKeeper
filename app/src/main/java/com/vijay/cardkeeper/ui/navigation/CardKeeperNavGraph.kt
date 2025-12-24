package com.vijay.cardkeeper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vijay.cardkeeper.ui.home.HomeScreen
import com.vijay.cardkeeper.ui.item.AddItemScreen

object CardKeeperDestinations {
    const val HOME_ROUTE = "home"
    const val ADD_ITEM_ROUTE = "add_item"
}

@Composable
fun CardKeeperNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = CardKeeperDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(route = CardKeeperDestinations.HOME_ROUTE) {
            HomeScreen(
                navigateToItemEntry = {
                    navController.navigate(CardKeeperDestinations.ADD_ITEM_ROUTE)
                }
            )
        }
        composable(route = CardKeeperDestinations.ADD_ITEM_ROUTE) {
            AddItemScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
