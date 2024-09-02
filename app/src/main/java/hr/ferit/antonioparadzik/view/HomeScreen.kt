package hr.ferit.antonioparadzik.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hr.ferit.antonioparadzik.navigation.HomeNavGraph
import hr.ferit.antonioparadzik.view.bottombar.BottomNavItems
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    rootNavController: NavHostController
) {

    val items = mutableListOf(
        BottomNavItems.HomeItem,
        BottomNavItems.AddProductItem,
        BottomNavItems.ProfileItem
    )
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.Black,
                contentColor = Color.White,
            ) {

                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    BottomNavigationItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(0.4f),
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        },
                        label = { Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp
                        ) }
                    )
                }
            }
        }
    ) {

        HomeNavGraph(
            navController = bottomNavController,
            rootNavController = rootNavController,
            homeViewModel = homeViewModel,
        )
    }
}