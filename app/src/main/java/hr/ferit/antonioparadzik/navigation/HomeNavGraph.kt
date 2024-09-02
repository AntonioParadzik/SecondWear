package hr.ferit.antonioparadzik.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.view.AddProductScreen
import hr.ferit.antonioparadzik.view.CameraScreen
import hr.ferit.antonioparadzik.view.EditProfileScreen
import hr.ferit.antonioparadzik.view.FeedScreen
import hr.ferit.antonioparadzik.view.FilterScreen
import hr.ferit.antonioparadzik.view.ProfileScreen
import hr.ferit.antonioparadzik.viewmodel.AddProductViewModel
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.CameraViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel
import hr.ferit.antonioparadzik.viewmodel.ProfileViewModel

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    homeViewModel: HomeViewModel,
) {
    //val homeViewModel: HomeViewModel = viewModel()
    val addProductViewModel: AddProductViewModel = viewModel()
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val cameraViewModel: CameraViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        route = ScreenRoutes.HomeNav.route,
        startDestination = ScreenRoutes.FeedScreen.route
    ) {
        composable(route = ScreenRoutes.FeedScreen.route){
            FeedScreen(navHostController = navController, homeViewModel, authenticationViewModel, profileViewModel, rootNavController)
        }

        composable(route = ScreenRoutes.FilterScreen.route){
            FilterScreen(navHostController = navController, homeViewModel = homeViewModel)
        }

        composable(route = ScreenRoutes.AddProductScreen.route){
            AddProductScreen(navHostController = navController, addProductViewModel)
        }

        composable(route = ScreenRoutes.ProfileScreen.route){
            ProfileScreen(navHostController = navController, profileViewModel, homeViewModel, authenticationViewModel, rootNavController)
        }

        composable(route = ScreenRoutes.EditProfileScreen.route){
            EditProfileScreen(navHostController = navController, profileViewModel)
        }

        composable(route = ScreenRoutes.CameraScreen.route){
            CameraScreen(navHostController = navController, cameraViewModel)
        }
    }
}