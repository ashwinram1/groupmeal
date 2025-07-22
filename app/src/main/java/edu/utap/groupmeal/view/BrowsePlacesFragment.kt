package edu.utap.groupmeal.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentPlacesBinding
import edu.utap.groupmeal.view.adapter.PlaceRowAdapter
import java.util.Locale

class BrowsePlacesFragment : Fragment(R.layout.fragment_places) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val viewModel: GooglePlacesViewModel by activityViewModels()

    private fun initAdapter(binding: FragmentPlacesBinding) {
        val placeRowAdapter = PlaceRowAdapter(viewModel) {
            val action =
                BrowsePlacesFragmentDirections.actionBrowsePlacesFragmentToOnePlaceFragment(it)
            findNavController().navigate(action)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = placeRowAdapter
        }

        viewModel.observePlaces().observe(viewLifecycleOwner) {
            placeRowAdapter.submitList(it)
        }
    }

    private fun initSwipeLayout(swipe: SwipeRefreshLayout) {
        swipe.setOnRefreshListener {
            viewModel.repoFetch()
            swipe.isRefreshing = false
        }
    }

    private fun setCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLong = "${it.latitude},${it.longitude}"
                    viewModel.setLocation(latLong)
                    viewModel.repoFetch()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("values", "here")
                    setCurrentLocation()
                }
            }
        super.onCreate(savedInstanceState)
        setCurrentLocation()
    }

    private fun getLatLng(context: Context, locationName: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(locationName, 1)
        if (!addresses.isNullOrEmpty()) {
            val location = addresses[0]
            return Pair(location.latitude, location.longitude)
        }
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPlacesBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        viewModel.showActionBarFavorites()
        initAdapter(binding)
        initSwipeLayout(binding.swipeRefreshLayout)
        binding.goButton.setOnClickListener {
            val location = binding.locationInput.text.toString().trim()
            val radiusStr = binding.radiusInput.text.toString().trim()
            val sortField = binding.sortFieldSpinner.selectedItem.toString().trim()
            val sortOrder = binding.sortOrderSpinner.selectedItem.toString().trim()
            val isOpenNow = binding.openNowCheckbox.isChecked

            var needToFetch = false

            if (location.isNotEmpty() && location.isNotBlank()) {
                val latLngPair = getLatLng(requireContext(), location)
                latLngPair?.let {
                    val latLngStr = "${it.first},${it.second}"
                    Log.d("latlng", latLngStr)
                    needToFetch = viewModel.setLocation(latLngStr) || needToFetch
                } ?: run {
                    Toast.makeText(requireContext(), "Location Not Found", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            if (radiusStr.isNotEmpty() && radiusStr.isNotBlank()) {
                val radius = radiusStr.toIntOrNull()
                if (radius != null) {
                    needToFetch = viewModel.setRadius(radius) || needToFetch
                }
            }

            if (sortField.isNotEmpty() && sortField.isNotBlank()) {
                needToFetch = viewModel.setSortField(sortField) || needToFetch
            }

            if (sortOrder.isNotEmpty() && sortOrder.isNotBlank()) {
                needToFetch = viewModel.setSortOrder(sortOrder) || needToFetch
            }

            needToFetch = viewModel.setIsOpenNow(isOpenNow) || needToFetch

            if (needToFetch) {
                viewModel.repoFetch()
            }
        }

        binding.targetButton.setOnClickListener {
            binding.locationInput.setText("")
            binding.radiusInput.setText("")
            setCurrentLocation()
        }

        binding.actionFavorite.setOnClickListener {
            val action = BrowsePlacesFragmentDirections.actionBrowsePlacesFragmentToPlacesFavorites()
            findNavController().navigate(action)
        }
    }
}