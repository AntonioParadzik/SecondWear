package hr.ferit.antonioparadzik.view.bottombar

import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.ScreenRoutes

sealed class BottomNavItems(
    val title : String,
    val route : String,
    val icon : Int
){
    data object HomeItem : BottomNavItems(
        title = "Home",
        route = ScreenRoutes.FeedScreen.route,
        icon = R.drawable.ic_home
    )

    data object AddProductItem : BottomNavItems(
        title = "Add Product",
        route = ScreenRoutes.AddProductScreen.route,
        icon = R.drawable.ic_add
    )

    data object ProfileItem : BottomNavItems(
        title = "Profile",
        route = ScreenRoutes.ProfileScreen.route,
        icon = R.drawable.ic_user
    )
}