package edu.utap.groupmeal.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.groupmeal.databinding.RowUserBinding
import edu.utap.groupmeal.model.User

class UserAdapter
    : ListAdapter<User, UserAdapter.VH>(UserDiff()) {

    class UserDiff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.email == newItem.email
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.friends == newItem.friends && oldItem.name == newItem.name
        }
    }

    inner class VH(private val binding: RowUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(u: User) {
            binding.tvUserName.text = u.name
            binding.tvUserEmail.text = u.email
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowUserBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

}