package till.messager.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import till.messager.data.model.AuthSession
import till.messager.data.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun signIn(email: String, password: String) {
        if (!isEmailValid(email)) {
            _state.value = LoginUiState.Error("Bitte gib eine gültige E-Mail-Adresse ein.")
            return
        }
        if (password.length < 6) {
            _state.value = LoginUiState.Error("Das Passwort muss mindestens 6 Zeichen lang sein.")
            return
        }
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            authRepository.signIn(email.trim(), password.trim())
                .onSuccess { _state.value = LoginUiState.Success(it) }
                .onFailure { _state.value = LoginUiState.Error(it.message ?: "Login fehlgeschlagen") }
        }
    }

    fun signUp(displayName: String, email: String, password: String) {
        if (displayName.isBlank()) {
            _state.value = LoginUiState.Error("Bitte gib einen Anzeigenamen ein.")
            return
        }
        if (!isEmailValid(email)) {
            _state.value = LoginUiState.Error("Bitte gib eine gültige E-Mail-Adresse für die Verifizierung ein.")
            return
        }
        if (password.length < 6) {
            _state.value = LoginUiState.Error("Das Passwort muss mindestens 6 Zeichen lang sein.")
            return
        }
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            authRepository.signUp(displayName.trim(), email.trim(), password.trim())
                .onSuccess { _state.value = LoginUiState.Success(it) }
                .onFailure { _state.value = LoginUiState.Error(it.message ?: "Registrierung fehlgeschlagen") }
        }
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val session: AuthSession) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
