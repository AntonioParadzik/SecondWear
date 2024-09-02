package hr.ferit.antonioparadzik

sealed class ScreenRoutes(val route: String) {
    //Screen routes
    data object LoginScreen : ScreenRoutes("login_screen")
    data object RegisterScreen : ScreenRoutes("register_screen")
    data object FeedScreen : ScreenRoutes("feed_screen")
    data object FilterScreen : ScreenRoutes("filter_screen")
    data object AddProductScreen : ScreenRoutes("addproduct_screen")
    data object ProfileScreen : ScreenRoutes("profile_screen")
    data object EditProfileScreen : ScreenRoutes("editprofile_screen")
    data object CameraScreen : ScreenRoutes("camera_screen")

    //Graph Routes
    data object AuthNav : ScreenRoutes("auth_nav_graph")
    data object HomeNav : ScreenRoutes("home_nav_graph")
}