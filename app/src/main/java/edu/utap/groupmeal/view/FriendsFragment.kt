package edu.utap.groupmeal.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.groupmeal.MainViewModel
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentFriendsBinding
import edu.utap.groupmeal.model.User
import edu.utap.groupmeal.toModelList
import edu.utap.groupmeal.view.adapter.SearchUserAdapter
import edu.utap.groupmeal.view.adapter.UserAdapter

class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")
        val binding = FragmentFriendsBinding.bind(view)
        binding.rvList.layoutManager = LinearLayoutManager(binding.rvList.context)
        binding.rvList.setHasFixedSize(true)
        val adapter = UserAdapter()

        binding.rvList.adapter = adapter
        fetchFriends(adapter)
        binding.svSearch.visibility = View.GONE

        val searchAdapter = SearchUserAdapter { user ->
            viewModel.addFriend(user) { ok, _ ->
                if (ok) {
                    Toast.makeText(context, "Added!", Toast.LENGTH_SHORT).show()
                    viewModel.refreshFriends()
                }
            }
        }

        viewModel.updateFriends.observe(viewLifecycleOwner) {
            viewModel.refresh { success, error ->
                if (error == null && success) {
                    viewModel.getLastUsersWithEmailLike().addOnSuccessListener { users ->
                        searchAdapter.submitList(users)
                    }
                }
            }
        }

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnMyFriends -> {
                    binding.rvList.adapter = adapter
                    fetchFriends(adapter)
                    styleSelectedButton(binding.btnMyFriends, binding.btnFindFriends)
                    binding.svSearch.visibility = View.GONE
                }

                R.id.btnFindFriends -> {
                    binding.rvList.adapter = searchAdapter
                    styleSelectedButton(binding.btnFindFriends, binding.btnMyFriends)
                    binding.svSearch.visibility = View.VISIBLE
                    binding.svSearch.setQuery("", true)
                }
            }
        }

        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(text: String): Boolean {
                if (!binding.btnMyFriends.isEnabled) {
                    fetchFriends(adapter)
                } else {
                    viewModel.getUsersWithEmailLike(text).addOnSuccessListener { users ->
                        searchAdapter.submitList(users)
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(text: String) = false
        })
    }

    private fun fetchFriends(adapter: UserAdapter) {
        viewModel.fetchCurrentFriends { friends, err ->
            if (err == null && friends != null) {
                friends
                    .toModelList<User>()
                    .addOnSuccessListener { f ->
                        adapter.submitList(f)
                    }
            }
        }
    }

    private fun styleSelectedButton(selected: Button, other: Button) {
        selected.isSelected = true
        other.isSelected = false
        val ctx = selected.context
        val black = ContextCompat.getColor(ctx, R.color.black)
        val white = ContextCompat.getColor(ctx, R.color.white)
        // Highlight the selected button: black bg, white text
        selected.backgroundTintList = ColorStateList.valueOf(black)
        selected.setTextColor(white)
        // Reset the other: white bg, black text
        other.backgroundTintList = ColorStateList.valueOf(white)
        other.setTextColor(black)
    }

}