package edu.utap.groupmeal

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

inline fun <reified T : Any> List<DocumentReference>.toModelList(): Task<List<T>> {
    if (isEmpty()) {
        return Tasks.forResult(emptyList())
    }
    val snaps: List<Task<DocumentSnapshot>> = map { it.get() }
    return Tasks
        .whenAllSuccess<DocumentSnapshot>(snaps)
        .continueWith { task ->
            task.result
                ?.mapNotNull { it.toObject(T::class.java) }
                ?: emptyList()
        }
}