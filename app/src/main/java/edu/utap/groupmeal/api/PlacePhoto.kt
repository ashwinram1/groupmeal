package edu.utap.groupmeal.api

import java.io.Serializable

data class PlacePhoto(
    val photo_reference: String,
    val height: Int?,
    val width: Int?
): Serializable