package edu.utap.groupmeal.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.groupmeal.databinding.RowAttendeeBinding
import edu.utap.groupmeal.model.Attendee

class AttendeeRowAdapter :
    ListAdapter<Attendee, AttendeeRowAdapter.VH>(AttendeeRowDiff()) {

    inner class VH(private val binding: RowAttendeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Attendee) {
            binding.tvName.text = item.user.name
            binding.tvEmail.text = item.user.email
            binding.tvStatus.text = item.isGoing.toString()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowAttendeeBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class AttendeeRowDiff : DiffUtil.ItemCallback<Attendee>() {
        override fun areItemsTheSame(a: Attendee, b: Attendee) =
            a.user.email == b.user.email

        override fun areContentsTheSame(a: Attendee, b: Attendee) =
            a == b
    }
}