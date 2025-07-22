package edu.utap.groupmeal.api

import android.util.Log

class GooglePlacesRepository(private val googlePlacesApi: GooglePlacesApi) {
    private fun unpackPlaces(response: GooglePlacesApi.PlacesResponse): List<GooglePlace> {
        return response.results.map { it }
    }

    private fun unpackPlace(response: GooglePlacesApi.PlaceResponse): GooglePlace {
        return response.result
    }

    suspend fun getRestaurants(
        location: String?,
        radius: Int?,
        sortField: String?,
        sortOrder: String?,
        isOpenNow: Boolean?
    ): List<GooglePlace> {
        val response: GooglePlacesApi.PlacesResponse?
        val rad = radius ?: 5
        response = googlePlacesApi.getNearbyRestaurants(
            location,
            rad * 1609
        )  // Change the radius later to something else (or the user can set this customly and so you will replace the hard-coded value with a variable)
        Log.d("GooglePlaces", response.toString())
        var result = unpackPlaces(response)
        if (!sortField.isNullOrEmpty() && sortField.isNotBlank()) {
            if (sortField == "Rating") {
                result = when (sortOrder) {
                    "Low to High" -> result.sortedBy { it.rating ?: 0f }
                    "High to Low" -> result.sortedByDescending { it.rating ?: 0f }
                    else -> result
                }
            } else if (sortField == "Price") {
                result = when (sortOrder) {
                    "Low to High" -> result.sortedBy { it.price_level ?: Int.MAX_VALUE }
                    "High to Low" -> result.sortedByDescending { it.price_level ?: Int.MIN_VALUE }
                    else -> result
                }
            }
        }
        return if (isOpenNow == true) {
            result.filter { it.opening_hours?.open_now == true }
        } else {
            result
        }
    }

    suspend fun getRestaurant(placesId: String): GooglePlace {
        val response: GooglePlacesApi.PlaceResponse?
        response = googlePlacesApi.getRestaurantDetails(placesId)
        Log.d("GooglePlace", response.toString())
        return unpackPlace(response)
    }
}