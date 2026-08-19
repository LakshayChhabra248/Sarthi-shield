package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoNavBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoTextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Cockpit : Screen("cockpit", "Cockpit", Icons.Filled.DeliveryDining)
    object FairWage : Screen("fair_wage", "Fair Pay", Icons.Filled.AccountBalanceWallet)
    object Shield : Screen("shield", "ID Shield", Icons.Filled.Shield)
    object VoiceSafety : Screen("voice_safety", "Voice Guard", Icons.Filled.Mic)
    object CityMap : Screen("city_map", "City Map", Icons.Filled.Map)
    object EdgeCore : Screen("edge_core", "Vision-IMU", Icons.Filled.Sensors)
}

val navItems = listOf(
    Screen.Cockpit,
    Screen.FairWage,
    Screen.Shield,
    Screen.VoiceSafety,
    Screen.CityMap,
    Screen.EdgeCore
)

@Composable
fun SarthiApp(sensorViewModel: SensorViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = BentoBackground,
        bottomBar = {
            NavigationBar(
                containerColor = BentoNavBackground,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title, modifier = Modifier.size(20.dp)) },
                        label = { Text(screen.title, fontSize = 9.sp, maxLines = 1) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPrimary,
                            selectedTextColor = BentoPrimary,
                            indicatorColor = BentoPrimaryContainer,
                            unselectedIconColor = BentoTextSecondary,
                            unselectedTextColor = BentoTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Cockpit.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Cockpit.route) {
                TripDeliveryScreen(sensorViewModel)
            }
            composable(Screen.FairWage.route) {
                EarningsFairWageScreen(sensorViewModel)
            }
            composable(Screen.Shield.route) {
                ShieldProtectionScreen(sensorViewModel)
            }
            composable(Screen.VoiceSafety.route) {
                VoiceSafetyScreen(sensorViewModel)
            }
            composable(Screen.CityMap.route) {
                CityMapScreen(sensorViewModel)
            }
            composable(Screen.EdgeCore.route) {
                VisionImuScreen(sensorViewModel)
            }
        }
    }
}
