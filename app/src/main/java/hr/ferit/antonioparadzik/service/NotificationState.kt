package hr.ferit.antonioparadzik.service

data class NotificationState(
    val remoteToken: String = "",
    val messageText: String = ""
)

data class SendMessageDto(
    val notification: NotificationBody
)

data class NotificationBody(
    val title: String,
    val body: String
)