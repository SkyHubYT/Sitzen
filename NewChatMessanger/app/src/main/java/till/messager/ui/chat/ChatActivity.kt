package till.messager.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import till.messager.TillMessagerApp
import till.messager.core.secureWindow
import till.messager.data.model.UserProfile
import till.messager.databinding.ActivityChatBinding
import till.messager.ui.common.SimpleViewModelFactory

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var contact: UserProfile
    private var chatId: String = ""

    private val viewModel: ChatViewModel by viewModels {
        val container = (application as TillMessagerApp).container
        SimpleViewModelFactory { ChatViewModel(container.authRepository, container.chatRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contact = UserProfile(
            uid = intent.getStringExtra(EXTRA_CONTACT_ID).orEmpty(),
            displayName = intent.getStringExtra(EXTRA_CONTACT_NAME).orEmpty(),
            email = intent.getStringExtra(EXTRA_CONTACT_EMAIL).orEmpty(),
            status = intent.getStringExtra(EXTRA_CONTACT_STATUS) ?: "Private by design",
            trustLabel = intent.getStringExtra(EXTRA_CONTACT_TRUST) ?: "Unverified"
        )

        binding.topBar.title = contact.displayName.ifBlank { "Secure Chat" }
        binding.topBar.subtitle = contact.status
        binding.topBar.setNavigationOnClickListener { finish() }

        adapter = MessageAdapter()
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recyclerMessages.adapter = adapter

        lifecycleScope.launch {
            viewModel.resolveChatId(contact)
                .onSuccess {
                    chatId = it
                    observeMessages()
                }
                .onFailure { Toast.makeText(this@ChatActivity, it.message, Toast.LENGTH_LONG).show() }
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.inputMessage.text?.toString().orEmpty().trim()
            if (text.isEmpty()) return@setOnClickListener
            lifecycleScope.launch {
                viewModel.send(chatId, contact, text)
                    .onSuccess { binding.inputMessage.setText("") }
                    .onFailure { Toast.makeText(this@ChatActivity, it.message, Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun observeMessages() {
        viewModel.startWatching(
            chatId = chatId,
            contact = contact,
            onChanged = {
                runOnUiThread {
                    adapter.submit(it)
                    if (it.isNotEmpty()) binding.recyclerMessages.scrollToPosition(it.lastIndex)
                }
            },
            onError = {
                runOnUiThread { Toast.makeText(this, it.message, Toast.LENGTH_LONG).show() }
            }
        )
    }

    companion object {
        const val EXTRA_CONTACT_ID = "extra_contact_id"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_CONTACT_EMAIL = "extra_contact_email"
        const val EXTRA_CONTACT_STATUS = "extra_contact_status"
        const val EXTRA_CONTACT_TRUST = "extra_contact_trust"
        const val EXTRA_FROM_THREAD_ID = "extra_from_thread_id"
    }
}
