package till.messager.ui.contacts

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
import till.messager.data.model.UserProfile
import till.messager.databinding.ActivityContactListBinding
import till.messager.ui.chat.ChatActivity
import till.messager.ui.common.SimpleViewModelFactory

class ContactListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactListBinding
    private lateinit var adapter: ContactAdapter

    private val viewModel: ContactsViewModel by viewModels {
        val container = (application as TillMessagerApp).container
        SimpleViewModelFactory { ContactsViewModel(container.authRepository, container.contactRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ContactAdapter(::openChat, ::addContact)
        binding.recyclerContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerContacts.adapter = adapter
        binding.topBar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            viewModel.ui.collect { state ->
                adapter.submit(state.contacts)
                binding.textHint.text = if (state.contacts.any { !it.isAddedContact }) {
                    "Tippe auf einen Nutzer, um ihn zu deinen Kontakten hinzuzufügen."
                } else {
                    "Alle sichtbaren Nutzer sind bereits in deinen Kontakten."
                }
                state.error?.let { Toast.makeText(this@ContactListActivity, it, Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun addContact(contact: UserProfile) {
        viewModel.addContact(contact)
        Toast.makeText(this, "${contact.displayName} wurde gespeichert", Toast.LENGTH_SHORT).show()
    }

    private fun openChat(contact: UserProfile) {
        startActivity(Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONTACT_ID, contact.uid)
            putExtra(ChatActivity.EXTRA_CONTACT_NAME, contact.displayName)
            putExtra(ChatActivity.EXTRA_CONTACT_EMAIL, contact.email)
            putExtra(ChatActivity.EXTRA_CONTACT_STATUS, contact.status)
            putExtra(ChatActivity.EXTRA_CONTACT_TRUST, contact.trustLabel)
        })
    }
}
