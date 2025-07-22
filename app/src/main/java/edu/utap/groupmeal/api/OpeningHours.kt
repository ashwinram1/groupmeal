package edu.utap.groupmeal.api

import java.io.Serializable

data class OpeningHours(
    val open_now: Boolean,
    val weekday_text: List<String>
): Serializable