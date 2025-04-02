package com.example.struku.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.struku.presentation.dashboard.DashboardScreen
import com.example.struku.presentation.detail.ReceiptDetailScreen
import com.example.struku.presentation.list.ReceiptListScreen
import com.example.struku.presentation.scan.ScanScreen
import com.example.struku.presentation.settings.SettingsScreen

object Screen {
    const val LIST = "list"
    const val SCAN = "scan"
    const val DETAIL = "detail"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"

    // For screens with arguments
    fun detailRoute(receiptId: Long) = "$DETAIL/$receiptId"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Receipt List Screen
        composable(Screen.LIST) {
            ReceiptListScreen(
                onNavigateToDetail = { receiptId ->
                    navController.navigate(Screen.detailRoute(receiptId))
                },
                onNavigateToScan = {
                    navController.navigate(Screen.SCAN)
                }
            )
        }

        // Receipt Scan Screen
        composable(Screen.SCAN) {
            ScanScreen(
                onScanComplete = { receiptId ->
                    // Pop back to list and then navigate to detail
                    navController.popBackStack()
                    navController.navigate(Screen.detailRoute(receiptId))
                }
            )
        }

        // Receipt Detail Screen
        composable(
            route = "${Screen.DETAIL}/{receiptId}",
            arguments = listOf(
                navArgument("receiptId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: 0L

            ReceiptDetailScreen(
                receiptId = receiptId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditComplete = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard Screen
        composable(Screen.DASHBOARD) {
            DashboardScreen(
                onNavigateToDetail = { receiptId ->
                    navController.navigate(Screen.detailRoute(receiptId))
                }
            )
        }

        // Settings Screen
        composable(Screen.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}