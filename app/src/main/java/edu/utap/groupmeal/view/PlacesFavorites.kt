package edu.utap.groupmeal.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentPlacesBinding
import edu.utap.groupmeal.view.adapter.PlaceRowAdapter

class PlacesFavorites: Fragment(R.layout.fragment_places) {
    private val viewModel: GooglePlacesViewModel by activityViewModels()

    private fun initAdapter(binding: FragmentPlacesBinding) {
        val postRowAdapter = PlaceRowAdapter(viewModel) {
            val action = PlacesFavoritesDirections.actionPlacesFavoritesToOnePlaceFragment(it)
            findNavController().navigate(action)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postRowAdapter
        }

        viewModel.observePlacesFavorites().observe(viewLifecycleOwner) {
            postRowAdapter.submitList(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")
        val binding = FragmentPlacesBinding.bind(view)
        viewModel.hideActionBarFavorites()
        initAdapter(binding)
        binding.swipeRefreshLayout.isEnabled = false
        binding.search.visibility = View.GONE
    }
}