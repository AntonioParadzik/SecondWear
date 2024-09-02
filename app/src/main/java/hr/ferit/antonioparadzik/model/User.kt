package hr.ferit.antonioparadzik.model

data class User(
    val username: String = "",
    val profileImageUrl: String = "",
    val userId: String = "",
    val email: String = "",
    val fcmToken: String = ""
)