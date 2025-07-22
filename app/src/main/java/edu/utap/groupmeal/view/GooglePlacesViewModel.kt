package edu.utap.groupmeal.view

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.groupmeal.api.GooglePlace
import edu.utap.groupmeal.api.GooglePlacesApi
import edu.utap.groupmeal.api.GooglePlacesRepository
import edu.utap.groupmeal.databinding.ActionBarBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GooglePlacesViewModel : ViewModel() {
    private var actionBarBinding: ActionBarBinding? = null

    private var location = MutableLiveData<String>()
    private var radius = MutableLiveData<Int>().apply {
        value = 5
    }
    private var sortField = MutableLiveData<String>()
    private var sortOrder = MutableLiveData<String>()
    private var isOpenNow = MutableLiveData<Boolean>()
    private val googlePlacesApi = GooglePlacesApi.create()
    private val googlePlacesRepository = GooglePlacesRepository(googlePlacesApi)
    private val googlePlacesFavoritesList = mutableListOf<GooglePlace>()

    private val onePlace = MutableLiveData<GooglePlace>()

    private var googlePlacesFavoritesLiveData = MutableLiveData<List<GooglePlace>>().apply {
        value = googlePlacesFavoritesList
    }

    private var netPlaces = MutableLiveData<List<GooglePlace>>()

    private var searchPlaces = MediatorLiveData<List<GooglePlace>>().apply {
        addSource(netPlaces) { places ->
            value = places
        }
    }

    private var searchGooglePlacesFavorites = MediatorLiveData<List<GooglePlace>>().apply {
        addSource(googlePlacesFavoritesLiveData) { currentFavorites ->
            value = currentFavorites
        }
    }

    fun observeOnePlace(): LiveData<GooglePlace> = onePlace

    fun fetchOnePlace(placeId: String) {
        viewModelScope.launch {
            val detailedPlace = googlePlacesRepository.getRestaurant(placeId)
            onePlace.postValue(detailedPlace)
        }
    }

    fun repoFetch() {
        val loc = location.value
        val rad = radius.value
        val sF = sortField.value
        val sO = sortOrder.value
        val iON = isOpenNow.value
        Log.d("values", loc.toString())
        Log.d("values", rad.toString())
        Log.d("values", sF.toString())
        Log.d("values", sO.toString())
        Log.d("values", iON.toString())

        viewModelScope.launch {
            val places = googlePlacesRepository.getRestaurants(loc, rad, sF, sO, iON)
            netPlaces.postValue(places)
        }
    }


    fun setRadius(newRadius: Int): Boolean {
        if (newRadius != radius.value) {
            radius.value = newRadius
            return true
        }
        return false
    }

    fun setSortField(newSortField: String): Boolean {
        if (newSortField != sortField.value) {
            sortField.value = newSortField
            return true
        }
        return false
    }

    fun setSortOrder(newSortOrder: String): Boolean {
        if (newSortOrder != sortOrder.value) {
            sortOrder.value = newSortOrder
            return true
        }
        return false
    }

    fun setIsOpenNow(newIsOpenNow: Boolean): Boolean {
        if (newIsOpenNow != isOpenNow.value) {
            isOpenNow.value = newIsOpenNow
            return true
        }
        return false
    }

    fun setLocation(newLocation: String): Boolean {
        if (newLocation != location.value) {
            location.value = newLocation
            return true
        }
        return false
    }

    fun observePlaces(): LiveData<List<GooglePlace>> {
        return searchPlaces
    }

    /////////////////////////
    // Action bar
    fun hideActionBarFavorites() {
        actionBarBinding?.actionFavorite?.visibility = View.GONE
    }

    fun showActionBarFavorites() {
        actionBarBinding?.actionFavorite?.visibility = View.VISIBLE
    }

    fun togglePlaceFavorite(place: GooglePlace) {
        viewModelScope.launch(Dispatchers.Main) {
            if (placeIsFavorite(place)) {
                googlePlacesFavoritesList.removeAll { it == place }
            } else {
                googlePlacesFavoritesList.add(place)
            }
            googlePlacesFavoritesLiveData.value = ArrayList(googlePlacesFavoritesList)
        }
    }

    fun placeIsFavorite(place: GooglePlace): Boolean {
        return googlePlacesFavoritesList.contains(place)
    }

    fun observePlacesFavorites(): LiveData<List<GooglePlace>> {
        return searchGooglePlacesFavorites
    }
}