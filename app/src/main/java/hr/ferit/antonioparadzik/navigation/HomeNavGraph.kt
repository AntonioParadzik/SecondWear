package hr.ferit.antonioparadzik.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.view.AddProductScreen
import hr.ferit.antonioparadzik.view.CameraScreen
import hr.ferit.antonioparadzik.view.EditProfileScreen
import hr.ferit.antonioparadzik.view.FeedScreen
import hr.ferit.antonioparadzik.view.FilterScreen
import hr.ferit.antonioparadzik.view.ProductScreen
import hr.ferit.antonioparadzik.view.ProfileScreen
import hr.ferit.antonioparadzik.viewmodel.AddProductViewModel
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    homeViewModel: HomeViewModel,

) {
    //val homeViewModel: HomeViewModel = viewModel()
    val addProductViewModel: AddProductViewModel = viewModel()
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    NavHost(
        navController = navController,
        route = ScreenRoutes.HomeNav.route,
        startDestination = ScreenRoutes.FeedScreen.route
    ) {
        composable(route = ScreenRoutes.FeedScreen.route){
            FeedScreen(navHostController = navController, homeViewModel, authenticationViewModel, rootNavController)
        }

        composable(route = ScreenRoutes.FilterScreen.route){
            FilterScreen(navHostController = navController)
        }

        composable(route = ScreenRoutes.ProductScreen.route){
            ProductScreen(navHostController = navController)
        }

        composable(route = ScreenRoutes.AddProductScreen.route){
            AddProductScreen(navHostController = navController, homeViewModel,addProductViewModel)
        }

        composable(route = ScreenRoutes.ProfileScreen.route){
            ProfileScreen(navHostController = navController)
        }

        composable(route = ScreenRoutes.EditProfileScreen.route){
            EditProfileScreen(navHostController = navController)
        }

        composable(route = ScreenRoutes.CameraScreen.route){
            CameraScreen(navHostController = navController)
        }
    }
}