package till.messager.core

import android.content.Context
import till.messager.data.repository.AuthRepository
import till.messager.data.repository.ChatRepository
import till.messager.data.repository.ContactRepository
import till.messager.data.source.local.AppDatabase
import till.messager.data.source.local.DemoAuthRepository
import till.messager.data.source.local.DemoChatRepository
import till.messager.data.source.local.DemoContactRepository
import till.messager.data.source.local.SessionManager
import till.messager.data.source.remote.FirebaseAuthRepository
import till.messager.data.source.remote.FirebaseChatRepository
import till.messager.data.source.remote.FirebaseContactRepository

class AppContainer(context: Context, firebaseEnabled: Boolean) {
    val backendMode: BackendMode = if (firebaseEnabled) BackendMode.FIREBASE else BackendMode.DEMO
    val sessionManager = SessionManager(context)
    private val database = AppDatabase.getDatabase(context)
    private val chatDao = database.chatDao()

    val authRepository: AuthRepository
    val contactRepository: ContactRepository
    val chatRepository: ChatRepository

    init {
        if (backendMode == BackendMode.FIREBASE) {
            authRepository = FirebaseAuthRepository(sessionManager)
            contactRepository = FirebaseContactRepository()
            chatRepository = FirebaseChatRepository(chatDao)
        } else {
            authRepository = DemoAuthRepository(sessionManager)
            contactRepository = DemoContactRepository()
            chatRepository = DemoChatRepository()
        }
    }
}
