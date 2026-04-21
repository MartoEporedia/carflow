package com.carflow.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carflow.app.ui.components.CarflowTab
import com.carflow.app.ui.components.CarflowTabBar
import com.carflow.app.ui.screens.ExpensesScreen
import com.carflow.app.ui.screens.HomeScreen
import com.carflow.app.ui.screens.ProfileScreen
import com.carflow.app.ui.screens.StatsScreen
import com.carflow.app.ui.screens.VehicleDetailScreen
import com.carflow.app.ui.screens.VehiclesScreen
import com.carflow.app.ui.viewmodel.CarflowViewModel

@Composable
fun CarflowNavGraph() {
    val navController = rememberNavController()
    val vm: CarflowViewModel = hiltViewModel()

    LaunchedEffect(Unit) { vm.seedDemoDataIfEmpty() }

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val showTabBar = currentRoute in listOf("home", "expenses", "vehicles", "stats")
    val currentTab = when (currentRoute) {
        "home"     -> CarflowTab.HOME
        "expenses" -> CarflowTab.EXPENSES
        "vehicles" -> CarflowTab.VEHICLES
        "stats"    -> CarflowTab.STATS
        else       -> CarflowTab.HOME
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
            ) {
                composable("home") {
                    HomeScreen(
                        vm = vm,
                        onNavStats = { navController.navigate("stats") },
                        onNavProfile = { navController.navigate("profile") },
                        onNavExpenses = { navController.navigate("expenses") },
                    )
                }
                composable("expenses") {
                    ExpensesScreen(vm = vm)
                }
                composable("vehicles") {
                    VehiclesScreen(
                        vm = vm,
                        onOpenVehicle = { id -> navController.navigate("vehicle/$id") },
                    )
                }
                composable("stats") {
                    StatsScreen(vm = vm)
                }
                composable(
                    route = "vehicle/{vehicleId}",
                    arguments = listOf(navArgument("vehicleId") { type = NavType.StringType }),
                ) { backStack ->
                    val id = backStack.arguments?.getString("vehicleId") ?: return@composable
                    VehicleDetailScreen(
                        vehicleId = id,
                        vm = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }

        if (showTabBar) {
            CarflowTabBar(
                current = currentTab,
                onNav = { tab ->
                    val route = when (tab) {
                        CarflowTab.HOME     -> "home"
                        CarflowTab.EXPENSES -> "expenses"
                        CarflowTab.VEHICLES -> "vehicles"
                        CarflowTab.STATS    -> "stats"
                        CarflowTab.ADD      -> "home" // FAB scrolls home to input
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
