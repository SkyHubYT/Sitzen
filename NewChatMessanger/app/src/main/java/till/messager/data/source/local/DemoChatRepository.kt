package till.messager.data.source.local

import kotlinx.coroutines.delay
import till.messager.data.model.AuthSession
import till.messager.data.model.ChatMessage
import till.messager.data.model.ChatThread
import till.messager.data.model.UserProfile
import till.messager.data.repository.ChatRepository
import till.messager.data.repository.CloseableSubscription
import java.util.concurrent.ConcurrentHashMap

class DemoChatRepository : ChatRepository {
    private val chatMessages = ConcurrentHashMap<String, MutableList<ChatMessage>>()

    override suspend fun loadThreads(session: AuthSession): Result<List<ChatThread>> {
        delay(150)
        val now = System.currentTimeMillis()
        return Result.success(
            listOf(
                ChatThread("demo-user_alice", "alice", "Alice Secure", "Ready for a private chat?", now - 60_000),
                ChatThread("demo-user_milo", "milo", "Milo Privacy", "Contacts and architecture are live.", now - 120_000)
            )
        )
    }

    override suspend fun resolveDirectChatId(session: AuthSession, contact: UserProfile): Result<String> {
        return Result.success(generateChatId(session.uid, contact.uid))
    }

    override fun watchMessages(
        session: AuthSession,
        chatId: String,
        contact: UserProfile,
        onChanged: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ): CloseableSubscription {
        val seed = chatMessages.getOrPut(chatId) {
            mutableListOf(
                ChatMessage("1", chatId, contact.uid, contact.displayName, "This is the V2 secure architecture preview.", System.currentTimeMillis() - 200000, false),
                ChatMessage("2", chatId, session.uid, session.displayName, "Real login, contacts and chat layers are prepared.", System.currentTimeMillis() - 100000, true)
            )
        }
        onChanged(seed.toList())
        return CloseableSubscription { }
    }

    override suspend fun sendMessage(session: AuthSession, chatId: String, contact: UserProfile, body: String): Result<Unit> {
        delay(80)
        val list = chatMessages.getOrPut(chatId) { mutableListOf() }
        list += ChatMessage(
            id = System.currentTimeMillis().toString(),
            chatId = chatId,
            senderId = session.uid,
            senderName = session.displayName,
            body = body,
            createdAtMillis = System.currentTimeMillis(),
            isMine = true
        )
        return Result.success(Unit)
    }

    private fun generateChatId(first: String, second: String): String {
        return listOf(first, second).sorted().joinToString("_")
    }
}
