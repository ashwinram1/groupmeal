package edu.utap.groupmeal.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.groupmeal.MainViewModel
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentInvitesBinding
import edu.utap.groupmeal.model.Invite
import edu.utap.groupmeal.toModelList
import edu.utap.groupmeal.view.adapter.InviteRowAdapter

class InvitesFragment : Fragment(R.layout.fragment_invites) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentInvitesBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        val rowAdapter = InviteRowAdapter {
            Log.d("doOneInvite", "invite ${it.id}")
            val action = InvitesFragmentDirections.actionInvitesToOneInvite(it)
            findNavController().navigate(action)
        }
        binding.recyclerView.adapter = rowAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.recyclerView.context)
        binding.recyclerView.setHasFixedSize(true)
        fetchInvites(rowAdapter)
    }

    private fun fetchInvites(adapter: InviteRowAdapter) {
        viewModel.fetchCurrentInvites { invites, err ->
            if (err == null && invites != null) {
                invites
                    .toModelList<Invite>()
                    .addOnSuccessListener { i ->
                        adapter.submitList(i)
                    }
            }
        }
    }

}