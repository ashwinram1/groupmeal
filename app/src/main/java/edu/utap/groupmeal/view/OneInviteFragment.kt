package edu.utap.groupmeal.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.groupmeal.MainViewModel
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentOneInviteBinding
import edu.utap.groupmeal.model.Attendee
import edu.utap.groupmeal.model.Invite
import edu.utap.groupmeal.model.Status
import edu.utap.groupmeal.model.User
import edu.utap.groupmeal.toModelList
import edu.utap.groupmeal.view.adapter.AttendeeRowAdapter

class OneInviteFragment : Fragment(R.layout.fragment_one_invite) {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: OneInviteFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")
        val binding = FragmentOneInviteBinding.bind(view)
        val invite = args.inviteArg
        binding.tvTime.text = invite.time?.toDate().toString()
        binding.tvUrl.text = invite.url
        binding.tvLocation.text = invite.location
        val adapter = AttendeeRowAdapter()
        binding.rvAttendees.adapter = adapter

        binding.btnAccept.setOnClickListener {
            viewModel.updateInvite(invite.id, Status.ACCEPTED) { success, _ ->
                if (success) {
                    viewModel.getInviteRef(invite).get().continueWith { result ->
                        result.result.toObject(Invite::class.java)
                            ?.let { it1 -> updateAttendees(it1, adapter, binding) }
                    }
                }
            }
        }

        binding.btnDecline.setOnClickListener {
            viewModel.updateInvite(invite.id, Status.DECLINED) { success, _ ->
                if (success) {
                    viewModel.getInviteRef(invite).get().continueWith { result ->
                        result.result.toObject(Invite::class.java)
                            ?.let { it1 -> updateAttendees(it1, adapter, binding) }
                    }
                }
            }
        }

        binding.rvAttendees.layoutManager = LinearLayoutManager(binding.rvAttendees.context)
        binding.rvAttendees.setHasFixedSize(true)

        updateAttendees(invite, adapter, binding)
    }

    private fun updateAttendees(
        invite: Invite,
        adapter: AttendeeRowAdapter,
        binding: FragmentOneInviteBinding
    ) {
        val refList = invite.members.keys.map { viewModel.getDB().document(it) }
        refList.toList().toModelList<User>().addOnSuccessListener { results ->
            val attendees = ArrayList<Attendee>()
            for (i in results.indices) {
                val user = results[i]
                val going = invite.members[refList[i].path]!!
                if (user.email == viewModel.getCurrentUserRef().id && going != Status.UNKNOWN) {
                    styleButtons(binding, going)
                }
                attendees.add(Attendee(user, going))
            }
            adapter.submitList(attendees)
        }
    }

    private fun styleButtons(binding: FragmentOneInviteBinding, status: Status) {
        val context = binding.root.context
        val accepted = status == Status.ACCEPTED
        val accentColor = ContextCompat.getColor(
            context,
            if (accepted) R.color.teal_700 else R.color.red_700
        )
        binding.btnAccept.backgroundTintList =
            ColorStateList.valueOf(if (accepted) accentColor else Color.TRANSPARENT)
        binding.btnDecline.backgroundTintList =
            ColorStateList.valueOf(if (!accepted) accentColor else Color.TRANSPARENT)
    }

}