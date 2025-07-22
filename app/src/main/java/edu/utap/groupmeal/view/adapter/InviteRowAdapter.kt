package edu.utap.groupmeal.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.groupmeal.databinding.RowInvitesBinding
import edu.utap.groupmeal.model.Invite

class InviteRowAdapter(
    private val navigateToOnePost: (Invite) -> Unit
) : ListAdapter<Invite, InviteRowAdapter.VH>(InviteDiff()) {

    inner class VH(private val binding: RowInvitesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(invite: Invite) {
            binding.tvInviteTime.text = invite.time?.toDate().toString()
            binding.tvInviteUrl.text = invite.url
            binding.tvInviteLocation.text = invite.location
            binding.root.setOnClickListener {
                navigateToOnePost(invite)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowInvitesBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class InviteDiff : DiffUtil.ItemCallback<Invite>() {
        override fun areItemsTheSame(oldItem: Invite, newItem: Invite): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Invite, newItem: Invite): Boolean {
            return oldItem.url == newItem.url && oldItem.time == newItem.time
                    && oldItem.location == newItem.location
                    && oldItem.members == newItem.members
        }
    }
}