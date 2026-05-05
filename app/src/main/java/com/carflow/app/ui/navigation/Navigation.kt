package com.carflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carflow.app.ui.screens.chat.ChatExpenseScreen
import com.carflow.app.ui.screens.expense.ExpenseInputScreen
import com.carflow.app.ui.screens.expense.ExpenseListScreen
import com.carflow.app.ui.screens.stats.StatsScreen
import com.carflow.app.ui.screens.vehicle.VehicleScreen

sealed class Screen(val route: String) {
    object ExpenseList : Screen("expense_list")
    object ExpenseInput : Screen("expense_input")
    object Stats : Screen("stats")
    object Vehicle : Screen("vehicle")
    object ChatExpense : Screen("chat_expense")
}

@Composable
fun CarFlowNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ExpenseList.route
    ) {
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateToInput = { navController.navigate(Screen.ExpenseInput.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToVehicle = { navController.navigate(Screen.Vehicle.route) },
                onNavigateToChat = { navController.navigate(Screen.ChatExpense.route) }
            )
        }
        composable(Screen.ExpenseInput.route) {
            ExpenseInputScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicle = { navController.navigate(Screen.Vehicle.route) }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Vehicle.route) {
            VehicleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ChatExpense.route) {
            ChatExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVehicle = { navController.navigate(Screen.Vehicle.route) }
            )
        }
    }
}
