package till.messager.data.source.local

import kotlinx.coroutines.delay
import till.messager.data.model.AuthSession
import till.messager.data.repository.AuthRepository
import java.util.UUID

class DemoAuthRepository(
    private val sessionManager: SessionManager
) : AuthRepository {

    override fun currentSession(): AuthSession? = sessionManager.read()

    override suspend fun signIn(email: String, password: String): Result<AuthSession> {
        delay(250)
        val session = AuthSession(
            uid = "demo-user",
            displayName = email.substringBefore('@').ifBlank { "Demo User" },
            email = email,
            isDemo = true
        )
        sessionManager.save(session)
        return Result.success(session)
    }

    override suspend fun signUp(displayName: String, email: String, password: String): Result<AuthSession> {
        delay(250)
        val session = AuthSession(
            uid = UUID.randomUUID().toString(),
            displayName = displayName,
            email = email,
            isDemo = true
        )
        sessionManager.save(session)
        return Result.success(session)
    }

    override suspend fun signOut() {
        sessionManager.clear()
    }
}
