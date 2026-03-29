package till.messager.data.repository

import till.messager.data.model.AuthSession
import till.messager.data.model.ChatMessage
import till.messager.data.model.ChatThread
import till.messager.data.model.UserProfile

interface ChatRepository {
    suspend fun loadThreads(session: AuthSession): Result<List<ChatThread>>
    suspend fun resolveDirectChatId(session: AuthSession, contact: UserProfile): Result<String>
    fun watchMessages(
        session: AuthSession,
        chatId: String,
        contact: UserProfile,
        onChanged: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ): CloseableSubscription
    suspend fun sendMessage(
        session: AuthSession,
        chatId: String,
        contact: UserProfile,
        body: String
    ): Result<Unit>
}

fun interface CloseableSubscription {
    fun cancel()
}
