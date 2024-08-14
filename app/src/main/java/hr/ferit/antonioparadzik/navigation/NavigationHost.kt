package hr.ferit.antonioparadzik.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.view.LoginScreen
import hr.ferit.antonioparadzik.view.RegisterScreen
import hr.ferit.antonioparadzik.view.bottombar.AddProductScreen
import hr.ferit.antonioparadzik.view.bottombar.AppScaffold
import hr.ferit.antonioparadzik.view.bottombar.HomeScreen
import hr.ferit.antonioparadzik.view.bottombar.ProfileScreen
import hr.ferit.antonioparadzik.viewmodel.AddProductViewModel
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun NavigationHost(
    navController: NavController
) {
    val homeViewModel: HomeViewModel = viewModel()
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val addProductViewModel: AddProductViewModel = viewModel()
    val currentUser = FirebaseAuth.getInstance().currentUser
    NavHost(
        navController = navController as NavHostController,
        startDestination = if (currentUser != null) "home_screen" else "login_screen"
    ) {
        composable("home_screen") {
            HomeScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                authenticationViewModel = authenticationViewModel
            )
        }
        composable("addproduct_screen") {
            AddProductScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                addProductViewModel = addProductViewModel

            )
        }
        composable("profile_screen") {
            ProfileScreen(
                navController = navController,
                homeViewModel = homeViewModel
            )
        }
        composable("login_screen") {
            LoginScreen(
                navController = navController,
                viewModel = authenticationViewModel
            )
        }
        composable("register_screen") {
            RegisterScreen(
                navController = navController,
                viewModel = authenticationViewModel
            )
        }

    }
}