package till.messager.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import till.messager.data.model.AuthSession
import till.messager.data.model.UserProfile
import till.messager.data.repository.AuthRepository
import till.messager.data.repository.ContactRepository

class ContactsViewModel(
    private val authRepository: AuthRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ContactsUiState())
    val ui: StateFlow<ContactsUiState> = _ui.asStateFlow()

    fun load() {
        val session = authRepository.currentSession() ?: return
        _ui.value = _ui.value.copy(session = session, loading = true)
        viewModelScope.launch {
            contactRepository.loadContacts(session.uid)
                .onSuccess { _ui.value = ContactsUiState(session, it, false, null) }
                .onFailure { _ui.value = ContactsUiState(session, emptyList(), false, it.message) }
        }
    }

    fun addContact(contact: UserProfile) {
        val session = _ui.value.session ?: authRepository.currentSession() ?: return
        viewModelScope.launch {
            contactRepository.addContact(session.uid, contact)
                .onSuccess { load() }
                .onFailure {
                    _ui.value = _ui.value.copy(error = it.message ?: "Kontakt konnte nicht gespeichert werden")
                }
        }
    }
}

data class ContactsUiState(
    val session: AuthSession? = null,
    val contacts: List<UserProfile> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
