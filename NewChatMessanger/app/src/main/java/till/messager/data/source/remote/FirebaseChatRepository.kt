package till.messager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import till.messager.data.model.AuthSession
import till.messager.data.model.ChatMessage
import till.messager.data.model.ChatThread
import till.messager.data.model.UserProfile
import till.messager.data.repository.ChatRepository
import till.messager.data.repository.CloseableSubscription
import till.messager.data.source.local.ChatDao

class FirebaseChatRepository(
    private val chatDao: ChatDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun loadThreads(session: AuthSession): Result<List<ChatThread>> = runCatching {
        // Return local data first
        val localThreads = chatDao.getChatThreads().first()
        
        // Fetch from remote and update local
        val users = firestore.collection("users").get().await().documents.associateBy { it.id }
        val remoteThreads = firestore.collection("directChats")
            .whereArrayContains("memberIds", session.uid)
            .get()
            .await()
            .documents
            .map { doc ->
                val memberIds = (doc.get("memberIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
                val partnerId = memberIds.firstOrNull { it != session.uid }.orEmpty()
                val partnerDoc = users[partnerId]
                val partnerName = partnerDoc?.getString("displayName") ?: "Secure contact"
                ChatThread(
                    id = doc.id,
                    partnerId = partnerId,
                    partnerName = partnerName,
                    lastMessagePreview = doc.getString("lastMessage") ?: "No messages yet",
                    updatedAtMillis = doc.getLong("updatedAtMillis") ?: 0L,
                    unreadCount = (doc.getLong("unread_${session.uid}") ?: 0L).toInt()
                )
            }
        
        chatDao.insertThreads(remoteThreads)
        remoteThreads.sortedByDescending { it.updatedAtMillis }
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
        // Observe local database
        val localJob = scope.launch {
            chatDao.getMessages(chatId).collect {
                onChanged(it)
            }
        }

        val registration: ListenerRegistration = firestore.collection("directChats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAtMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    val senderId = doc.getString("senderId").orEmpty()
                    ChatMessage(
                        id = doc.id,
                        chatId = chatId,
                        senderId = senderId,
                        senderName = doc.getString("senderName") ?: if (senderId == session.uid) session.displayName else contact.displayName,
                        body = doc.getString("body") ?: "",
                        createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                        isMine = senderId == session.uid
                    )
                }
                // Save to local database
                scope.launch {
                    chatDao.insertMessages(items)
                }
            }
        return CloseableSubscription { 
            registration.remove()
            localJob.cancel()
        }
    }

    override suspend fun sendMessage(session: AuthSession, chatId: String, contact: UserProfile, body: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val tempId = "temp_${now}"
        
        // Optimistic update locally
        val tempMessage = ChatMessage(
            id = tempId,
            chatId = chatId,
            senderId = session.uid,
            senderName = session.displayName,
            body = body,
            createdAtMillis = now,
            isMine = true
        )
        chatDao.insertMessage(tempMessage)

        val chatRef = firestore.collection("directChats").document(chatId)
        val payload = hashMapOf(
            "memberIds" to listOf(session.uid, contact.uid),
            "lastMessage" to body,
            "updatedAtMillis" to now,
            "unread_${contact.uid}" to 1,
            "unread_${session.uid}" to 0
        )
        chatRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
        
        val docRef = chatRef.collection("messages").add(
            hashMapOf(
                "senderId" to session.uid,
                "senderName" to session.displayName,
                "body" to body,
                "createdAtMillis" to now
            )
        ).await()

        // Replace temp message with real one from remote
        // In this implementation, the snapshot listener will handle the sync
    }

    private fun generateChatId(first: String, second: String): String {
        return listOf(first, second).sorted().joinToString("_")
    }
}
