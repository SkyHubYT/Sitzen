package till.messager.data.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val status: String = "Sicher verschlüsselt",
    val trustLabel: String = "Benutzer",
    val lastSeenMillis: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false,
    val registrationDate: Long = System.currentTimeMillis(),
    val isAddedContact: Boolean = false,
    val proPlan: String = "free"
)
