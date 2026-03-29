package till.messager.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import till.messager.data.model.UserProfile
import till.messager.databinding.ItemContactBinding

class ContactAdapter(
    private val onOpenChat: (UserProfile) -> Unit,
    private val onAddContact: (UserProfile) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private val items = mutableListOf<UserProfile>()

    fun submit(data: List<UserProfile>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) = holder.bind(items[position])

    inner class ContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserProfile) {
            binding.textName.text = item.displayName
            binding.textEmail.text = item.email
            binding.textStatus.text = item.status
            binding.textTrust.text = buildString {
                append(item.trustLabel)
                if (item.proPlan != "free") append(" • ${item.proPlan.uppercase()}")
            }

            val actionText = if (item.isAddedContact) "Chat öffnen" else "Kontakt hinzufügen"
            binding.buttonAction.text = actionText
            binding.buttonAction.setOnClickListener {
                if (item.isAddedContact) onOpenChat(item) else onAddContact(item)
            }
            binding.root.setOnClickListener {
                if (item.isAddedContact) onOpenChat(item) else onAddContact(item)
            }
        }
    }
}
