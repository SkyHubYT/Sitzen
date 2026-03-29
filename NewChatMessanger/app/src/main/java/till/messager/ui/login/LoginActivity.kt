package till.messager.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import till.messager.TillMessagerApp
import till.messager.core.secureWindow
import till.messager.databinding.ActivityLoginBinding
import till.messager.ui.common.SimpleViewModelFactory
import till.messager.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        val container = (application as TillMessagerApp).container
        SimpleViewModelFactory { LoginViewModel(container.authRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val container = (application as TillMessagerApp).container
        binding.textBackend.text = if (container.backendMode.name == "DEMO") {
            "Demo-Modus aktiv: Ohne google-services.json startet die App als sichere Architektur-Preview."
        } else {
            "Firebase aktiv: Echte Anmeldung, Kontakte und Chats sind verfügbar."
        }

        binding.buttonSignIn.setOnClickListener {
            viewModel.signIn(
                binding.inputEmail.text?.toString().orEmpty(),
                binding.inputPassword.text?.toString().orEmpty()
            )
        }
        binding.buttonRegister.setOnClickListener {
            viewModel.signUp(
                binding.inputDisplayName.text?.toString().orEmpty(),
                binding.inputEmail.text?.toString().orEmpty(),
                binding.inputPassword.text?.toString().orEmpty()
            )
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.visibility = if (state is LoginUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
                when (state) {
                    is LoginUiState.Error -> Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    is LoginUiState.Success -> {
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                    else -> Unit
                }
            }
        }
    }
}
