package till.messager.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import till.messager.data.model.ChatMessage
import till.messager.databinding.ItemMessageIncomingBinding
import till.messager.databinding.ItemMessageOutgoingBinding
import java.text.DateFormat
import java.util.Date

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ChatMessage>()

    fun submit(data: List<ChatMessage>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = if (items[position].isMine) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            OutgoingHolder(ItemMessageOutgoingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            IncomingHolder(ItemMessageIncomingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IncomingHolder -> holder.bind(items[position])
            is OutgoingHolder -> holder.bind(items[position])
        }
    }

    private fun format(time: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(time))

    inner class IncomingHolder(private val binding: ItemMessageIncomingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.textBody.text = item.body
            binding.textMeta.text = "${item.senderName} · ${format(item.createdAtMillis)}"
        }
    }

    inner class OutgoingHolder(private val binding: ItemMessageOutgoingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.textBody.text = item.body
            binding.textMeta.text = format(item.createdAtMillis)
        }
    }
}
