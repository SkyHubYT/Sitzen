package till.messager.data.repository

import till.messager.data.model.AuthSession

interface AuthRepository {
    fun currentSession(): AuthSession?
    suspend fun signIn(email: String, password: String): Result<AuthSession>
    suspend fun signUp(displayName: String, email: String, password: String): Result<AuthSession>
    suspend fun signOut()
}
