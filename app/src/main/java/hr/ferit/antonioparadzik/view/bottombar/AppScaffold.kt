package hr.ferit.antonioparadzik.view.bottombar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.navigation.NavigationHost


@Composable
fun AppScaffold(
    navController: NavController,
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val currentUser = FirebaseAuth.getInstance().currentUser


    Scaffold(
        bottomBar = {
            BottomNav(navController = navController)
        },
        scaffoldState = scaffoldState,
        content = { contentPadding ->
            Box(modifier = Modifier.padding(contentPadding)) {
                if (currentUser != null) {
                    NavigationHost(navController = navController)
                } else {
                    // Navigate back to the NavHost in MainActivity
                    LaunchedEffect(Unit) {
                        navController.navigate("login_screen") {
                            popUpTo("app_scaffold") { inclusive = true }
                        }
                    }
                }
            }
        }
    )
}