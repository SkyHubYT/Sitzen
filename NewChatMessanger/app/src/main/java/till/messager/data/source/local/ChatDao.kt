package till.messager.data.source.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import till.messager.data.model.ChatMessage
import till.messager.data.model.ChatThread

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY updatedAtMillis DESC")
    fun getChatThreads(): Flow<List<ChatThread>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ChatThread>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAtMillis ASC")
    fun getMessages(chatId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
}
