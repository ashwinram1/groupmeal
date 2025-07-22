package edu.utap.groupmeal.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import edu.utap.groupmeal.MainViewModel
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment :
    Fragment(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModels()

    private fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.getAction(direction.actionId)?.run {
            navigate(direction)
        }
    }

    // No need for onCreateView because we passed R.layout to Fragment constructor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        viewModel.currentUserAuthorization.observe(viewLifecycleOwner) { user ->
            binding.tvGreeting.text = String.format("Welcome ${user.name}!")
        }
        binding.btnGroupsFriends.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToFriends()
            findNavController().safeNavigate(action)
        }
        binding.btnBrowseRestaurants.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToBrowsePlacesFragment()
            findNavController().safeNavigate(action)
        }
        binding.btnInvites.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToInvites()
            findNavController().safeNavigate(action)
        }
    }
}