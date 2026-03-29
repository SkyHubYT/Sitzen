package till.messager.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import till.messager.data.model.ChatThread
import till.messager.databinding.ItemThreadBinding
import java.text.DateFormat
import java.util.Date

class RecentChatsAdapter(
    private val onClick: (ChatThread) -> Unit
) : RecyclerView.Adapter<RecentChatsAdapter.ThreadViewHolder>() {
    private val items = mutableListOf<ChatThread>()

    fun submit(data: List<ChatThread>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val binding = ItemThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThreadViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) = holder.bind(items[position])

    inner class ThreadViewHolder(private val binding: ItemThreadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatThread) {
            binding.textTitle.text = item.partnerName
            binding.textSubtitle.text = item.lastMessagePreview
            binding.textMeta.text = if (item.updatedAtMillis > 0) {
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.updatedAtMillis))
            } else {
                "Noch kein Verlauf"
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
