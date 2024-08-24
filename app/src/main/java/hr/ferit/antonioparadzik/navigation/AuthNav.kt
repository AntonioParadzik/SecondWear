package hr.ferit.antonioparadzik.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.view.LoginScreen
import hr.ferit.antonioparadzik.view.RegisterScreen
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel

fun NavGraphBuilder.AuthNav(
    navController: NavHostController,
    authenticationViewModel: AuthenticationViewModel
){
    navigation(
        startDestination = ScreenRoutes.LoginScreen.route,
        route = ScreenRoutes.AuthNav.route
    ) {
        composable(route = ScreenRoutes.LoginScreen.route) {
            LoginScreen(navController = navController, viewModel = authenticationViewModel)
        }
        composable(route = ScreenRoutes.RegisterScreen.route) {
            RegisterScreen(navController = navController, viewModel = authenticationViewModel)
        }
    }
}