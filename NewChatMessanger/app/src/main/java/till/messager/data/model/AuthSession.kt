package till.messager.data.model

data class AuthSession(
    val uid: String,
    val displayName: String,
    val email: String,
    val isDemo: Boolean,
    val isEmailVerified: Boolean = false
)
