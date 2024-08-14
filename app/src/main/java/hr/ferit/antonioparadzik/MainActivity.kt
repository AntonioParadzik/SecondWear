package hr.ferit.antonioparadzik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.ui.theme.SecondWearTheme
import hr.ferit.antonioparadzik.view.bottombar.AppScaffold
import hr.ferit.antonioparadzik.view.LoginScreen
import hr.ferit.antonioparadzik.view.RegisterScreen
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val navController = rememberNavController()

            SecondWearTheme {
                NavHost(navController = navController, startDestination = if (currentUser != null) "app_scaffold" else "login_screen") {
                    composable("login_screen"){
                        LoginScreen(navController = navController, viewModel = authenticationViewModel)
                    }
                    composable("register_screen"){
                        RegisterScreen(navController = navController, viewModel = authenticationViewModel)
                    }
                    composable("app_scaffold") {

                        AppScaffold(navController = navController)

                    }
                }
            }
        }
    }
}


