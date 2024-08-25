package hr.ferit.antonioparadzik.model

data class Post(
    val name: String = "",
    val size: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = "",
    val username: String = ""
)