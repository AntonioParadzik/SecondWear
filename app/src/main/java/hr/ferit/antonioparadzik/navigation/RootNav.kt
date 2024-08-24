package hr.ferit.antonioparadzik.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.view.HomeScreen
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun RootNav(
    currentUser : Any?,
    authenticationViewModel: AuthenticationViewModel,
    homeViewModel: HomeViewModel
) {
    val rootNavController = rememberNavController()
    val context = LocalContext.current
    NavHost(navController = rootNavController, startDestination = if (currentUser != null) ScreenRoutes.HomeNav.route else ScreenRoutes.AuthNav.route) {
        AuthNav(navController = rootNavController, authenticationViewModel)

        composable(route = ScreenRoutes.HomeNav.route) {
            HomeScreen(
                homeViewModel,
                rootNavController
            )
        }
    }
}