package edu.utap.groupmeal.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.gson.annotations.SerializedName
import java.io.Serializable

enum class Status {
    UNKNOWN,
    ACCEPTED,
    DECLINED
}

data class Invite(
    @SerializedName("id")
    @DocumentId var id: String = "",
    @SerializedName("time")
    var time: Timestamp? = null,
    @SerializedName("location")
    var location: String = "",
    @SerializedName("url")
    var url: String = "",
    @SerializedName("members")
    var members: Map<String, Status> = emptyMap(),
) : Serializable