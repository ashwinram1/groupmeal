package edu.utap.groupmeal.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class User(
    @DocumentId var email: String = "",
    var name: String = "",
    var friends: List<DocumentReference> = emptyList(),
    var invites: List<DocumentReference> = emptyList(),
)