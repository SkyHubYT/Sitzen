package till.messager.ui.chats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import till.messager.TillMessagerApp
import till.messager.core.secureWindow
import till.messager.data.model.ChatThread
import till.messager.databinding.ActivityChatListBinding
import till.messager.ui.chat.ChatActivity
import till.messager.ui.common.SimpleViewModelFactory
import till.messager.ui.home.RecentChatsAdapter

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: RecentChatsAdapter

    private val viewModel: ChatListViewModel by viewModels {
        val container = (application as TillMessagerApp).container
        SimpleViewModelFactory { ChatListViewModel(container.authRepository, container.chatRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecentChatsAdapter(::openThread)
        binding.recyclerThreads.layoutManager = LinearLayoutManager(this)
        binding.recyclerThreads.adapter = adapter
        binding.topBar.setNavigationOnClickListener { finish() }

        intent.getStringExtra(EXTRA_DIRECT_ID)?.let { id ->
            val name = intent.getStringExtra(EXTRA_DIRECT_NAME).orEmpty()
            openThread(ChatThread(id, intent.getStringExtra(EXTRA_DIRECT_PARTNER_ID).orEmpty(), name, "", 0))
            finish()
            return
        }

        lifecycleScope.launch {
            viewModel.ui.collect { state ->
                adapter.submit(state.threads)
                state.error?.let { Toast.makeText(this@ChatListActivity, it, Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun openThread(thread: ChatThread) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONTACT_ID, thread.partnerId)
            putExtra(ChatActivity.EXTRA_CONTACT_NAME, thread.partnerName)
            putExtra(ChatActivity.EXTRA_FROM_THREAD_ID, thread.id)
        })
    }

    companion object {
        private const val EXTRA_DIRECT_ID = "extra_direct_id"
        private const val EXTRA_DIRECT_NAME = "extra_direct_name"
        private const val EXTRA_DIRECT_PARTNER_ID = "extra_direct_partner_id"

        fun intentToDirect(context: Context, partnerId: String, partnerName: String): Intent {
            return Intent(context, ChatListActivity::class.java).apply {
                putExtra(EXTRA_DIRECT_ID, "")
                putExtra(EXTRA_DIRECT_PARTNER_ID, partnerId)
                putExtra(EXTRA_DIRECT_NAME, partnerName)
            }
        }
    }
}
