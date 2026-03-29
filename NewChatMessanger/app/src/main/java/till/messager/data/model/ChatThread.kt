package till.messager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val id: String,
    val partnerId: String,
    val partnerName: String,
    val lastMessagePreview: String,
    val updatedAtMillis: Long,
    val unreadCount: Int = 0
)
