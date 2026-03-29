package till.messager.ui.home

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
import till.messager.databinding.ActivityHomeBinding
import till.messager.ui.chats.ChatListActivity
import till.messager.ui.common.SimpleViewModelFactory
import till.messager.ui.contacts.ContactListActivity
import till.messager.ui.login.LoginActivity
import till.messager.ui.privacy.PrivacyCenterActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: RecentChatsAdapter

    private val viewModel: HomeViewModel by viewModels {
        val container = (application as TillMessagerApp).container
        SimpleViewModelFactory { HomeViewModel(container.authRepository, container.chatRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecentChatsAdapter(::openThread)
        binding.recyclerRecentChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentChats.adapter = adapter

        binding.buttonContacts.setOnClickListener { startActivity(Intent(this, ContactListActivity::class.java)) }
        binding.buttonChats.setOnClickListener { startActivity(Intent(this, ChatListActivity::class.java)) }
        binding.buttonPrivacy.setOnClickListener { startActivity(Intent(this, PrivacyCenterActivity::class.java)) }
        binding.buttonLogout.setOnClickListener {
            lifecycleScope.launch {
                viewModel.signOut()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finishAffinity()
            }
        }

        lifecycleScope.launch {
            viewModel.ui.collect { state ->
                val session = state.session
                if (session != null && !session.isDemo && !session.isEmailVerified) {
                    Toast.makeText(this@HomeActivity, "Bitte bestätige zuerst deine E-Mail, um alle Funktionen zu nutzen.", Toast.LENGTH_LONG).show()
                    viewModel.signOut()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                    return@collect
                }

                binding.textGreeting.text = session?.let { "Hallo ${it.displayName}" } ?: "Hallo"
                binding.textSub.text = when {
                    session == null -> "Keine aktive Sitzung"
                    session.isDemo -> "Demo-Modus ohne Firebase-Konfiguration"
                    else -> "Firebase verbunden · Realtime Chat · Kontakte · Beta v6"
                }

                val isAdmin = session?.email.equals("tillscheidegget@gmail.com", ignoreCase = true)
                binding.textAdminInfo.text = if (isAdmin) {
                    "Admin erkannt: Diese Mail erhält Lifetime-Pro, Admin-Rechte und kann Pro-Versionen verteilen."
                } else {
                    "Pro-Pläne vorbereitet: CHF 4 monatlich · CHF 40 jährlich · CHF 50 Lifetime"
                }
                binding.textPlanInfo.text = if (isAdmin) {
                    "Beta v6 Final · Rolle: System-Administrator · Plan: Lifetime · Firebase online"
                } else {
                    "Beta v6 Final mit Firebase-Login, E-Mail-Verifizierung, Kontaktlisten und vorbereitetem Billing-Flow."
                }
                adapter.submit(state.recentChats)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun openThread(thread: ChatThread) {
        startActivity(ChatListActivity.intentToDirect(this, thread.partnerId, thread.partnerName))
    }
}
