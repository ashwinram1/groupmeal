package edu.utap.groupmeal.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyRestaurants(
        @Query("location") location: String?,  // "30.2672,-97.7431"
        @Query("radius") radius: Int?,        // in meters
        @Query("type") type: String = "restaurant",
        @Query("key") apiKey: String = API_KEY
    ): PlacesResponse

    @GET("maps/api/place/details/json")
    suspend fun getRestaurantDetails(
        @Query("place_id") place_id: String,
        @Query("fields") fields: String = "name,website,formatted_address,opening_hours,international_phone_number,url,photos",
        @Query("key") apiKey: String = API_KEY
    ): PlaceResponse

    data class PlacesResponse(val results: List<GooglePlace>)
    data class PlaceResponse(val result: GooglePlace)

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/"
        private const val API_KEY = "AIzaSyDZb5T5praR793eAhVr4ylMjD0Xlb12bec"

        fun create(): GooglePlacesApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GooglePlacesApi::class.java)
        }
    }
}
