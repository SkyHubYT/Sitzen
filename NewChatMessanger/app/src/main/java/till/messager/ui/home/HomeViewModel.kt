package till.messager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import till.messager.data.model.AuthSession
import till.messager.data.model.ChatThread
import till.messager.data.repository.AuthRepository
import till.messager.data.repository.ChatRepository

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    fun load() {
        val session = authRepository.currentSession() ?: return
        _ui.value = _ui.value.copy(session = session, loading = true)
        viewModelScope.launch {
            chatRepository.loadThreads(session)
                .onSuccess { _ui.value = HomeUiState(session, it, false, null) }
                .onFailure { _ui.value = HomeUiState(session, emptyList(), false, it.message) }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}

data class HomeUiState(
    val session: AuthSession? = null,
    val recentChats: List<ChatThread> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
