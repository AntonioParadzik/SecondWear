package hr.ferit.antonioparadzik

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")

    object Login : Screen("login_screen")
}