package till.messager.ui.chat

import androidx.lifecycle.ViewModel
import till.messager.data.model.AuthSession
import till.messager.data.model.ChatMessage
import till.messager.data.model.UserProfile
import till.messager.data.repository.AuthRepository
import till.messager.data.repository.ChatRepository
import till.messager.data.repository.CloseableSubscription

class ChatViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private var subscription: CloseableSubscription? = null

    fun currentSession(): AuthSession? = authRepository.currentSession()

    suspend fun resolveChatId(contact: UserProfile): Result<String> {
        val session = currentSession() ?: return Result.failure(IllegalStateException("Keine Session"))
        return chatRepository.resolveDirectChatId(session, contact)
    }

    fun startWatching(
        chatId: String,
        contact: UserProfile,
        onChanged: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val session = currentSession() ?: return
        subscription?.cancel()
        subscription = chatRepository.watchMessages(session, chatId, contact, onChanged, onError)
    }

    suspend fun send(chatId: String, contact: UserProfile, body: String): Result<Unit> {
        val session = currentSession() ?: return Result.failure(IllegalStateException("Keine Session"))
        return chatRepository.sendMessage(session, chatId, contact, body)
    }

    override fun onCleared() {
        subscription?.cancel()
        super.onCleared()
    }
}
