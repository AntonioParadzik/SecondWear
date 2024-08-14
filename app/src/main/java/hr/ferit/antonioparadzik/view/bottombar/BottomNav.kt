package hr.ferit.antonioparadzik.view.bottombar

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import hr.ferit.antonioparadzik.navigation.NavigationScreen

@Composable
fun BottomNav(navController: NavController){
    val items = listOf(
        NavigationScreen.HomeScreen,
        NavigationScreen.AddProductScreen,
        NavigationScreen.ProfileScreen
    )
    BottomNavigation {
        val navBackStackEntry = navController.currentBackStackEntry
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}