package edu.utap.groupmeal.api

import java.io.Serializable

data class GooglePlace(
    val name: String,
    val vicinity: String?,
    val rating: Float?,
    val price_level: Int?,
    val user_ratings_total: Int?,
    var place_id: String,
    val photos: List<PlacePhoto>? = null,
    val opening_hours: OpeningHours? = null,
    val website: String?,
    val formatted_address: String?,
    val url: String?,
    val international_phone_number: String?
): Serializable {
    override fun equals(other: Any?) : Boolean =
        if (other is GooglePlace) {
            place_id == other.place_id
        } else {
            false
        }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (vicinity?.hashCode() ?: 0)
        result = 31 * result + (rating?.hashCode() ?: 0)
        result = 31 * result + (user_ratings_total ?: 0)
        result = 31 * result + place_id.hashCode()
        result = 31 * result + (photos?.hashCode() ?: 0)
        return result
    }
}
