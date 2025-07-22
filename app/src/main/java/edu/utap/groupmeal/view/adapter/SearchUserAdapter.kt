package edu.utap.groupmeal.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.groupmeal.databinding.RowUserSearchBinding
import edu.utap.groupmeal.model.User
import edu.utap.groupmeal.view.adapter.UserAdapter.UserDiff

class SearchUserAdapter(private val onAdd: (User) -> Unit) :
    ListAdapter<User, SearchUserAdapter.VH>(
        UserDiff()
    ) {

    inner class VH(private val binding: RowUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(u: User) {
            binding.tvUserName.text = u.name
            binding.tvUserEmail.text = u.email
            binding.btnAction.text = "ADD"
            binding.btnAction.setOnClickListener { onAdd(u) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserAdapter.VH {
        val binding =
            RowUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: SearchUserAdapter.VH, position: Int) {
        holder.bind(getItem(position))
    }


}