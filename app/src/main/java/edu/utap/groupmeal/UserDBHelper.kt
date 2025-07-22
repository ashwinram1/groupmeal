package edu.utap.groupmeal

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.utap.groupmeal.model.Invite
import edu.utap.groupmeal.model.Status
import edu.utap.groupmeal.model.User

class UserDBHelper {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userCollection = "allUsers"
    private val inviteCollection = "allInvites"

    fun getRefFromEmail(email: String): DocumentReference {
        return db.collection(userCollection).document(email)
    }

    fun getDB(): FirebaseFirestore {
        return db
    }

    fun getInvite(id : String) : DocumentReference {
        return db.collection(inviteCollection).document(id)
    }

    fun addFriend(
        currUserEmail: String,
        friendEmail: String,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        val userRef = db.collection(userCollection).document(currUserEmail)
        val friendRef = db.collection(userCollection).document(friendEmail)
        val batch = db.batch()
        batch.update(userRef, "friends", FieldValue.arrayUnion(friendRef))
        batch.update(friendRef, "friends", FieldValue.arrayUnion(userRef))
        batch.commit()
            .addOnSuccessListener {
                Log.i("Firestore", "addFriend succeeded")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "addFriend failed", e)
                onComplete(false, e)
            }
    }

    fun sendInvite(
        time: Timestamp,
        location: String,
        url: String,
        members: Map<String, Status>,
    ) {
        val inviteRef = db.collection(inviteCollection).document()

        val batch = db.batch()
        val invite = Invite(
            id = inviteRef.id,
            time = time,
            location = location,
            url = url,
            members = members,
        )
        batch.set(inviteRef, invite)
        members.keys.forEach { docPath ->
            batch.update(db.document(docPath), "invites", FieldValue.arrayUnion(inviteRef))
        }
        batch.commit()
            .addOnFailureListener { e ->
                Log.e("Firestore", "sendInvite failed", e)
            }
    }

    fun respondToInvite(
        email: String,
        inviteId: String,
        response: Status,
        onComplete: (success: Boolean, error: Exception?) -> Unit
    ) {
        val inviteRef = db.collection(inviteCollection).document(inviteId)
        val currRef = db.collection(userCollection).document(email)
        db.runTransaction { tx ->
            val snap = tx.get(inviteRef)
            val current = snap.toObject(Invite::class.java)
                ?: throw FirebaseFirestoreException(
                    "Invite not found",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )

            if (!current.members.containsKey(currRef.path)) {
                throw FirebaseFirestoreException(
                    "Not invited",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }

            val updated = HashMap(current.members)
            updated[currRef.path] = response

            tx.update(inviteRef, "members", updated)
        }.addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e ->
                val isDenied = e is FirebaseFirestoreException &&
                        e.code ==
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                onComplete(false, if (isDenied) null else e)
            }
    }

    fun queryUnknownUsers(q: String, currentUser: User): Task<List<User>> {
        val friendEmails = currentUser.friends.map { it.id }
        var query = db.collection(userCollection).orderBy(FieldPath.documentId())
        if (q.isNotEmpty()) {
            query = query.startAt(q).endAt("$q\uf8ff")
        }
        return query
            .get()
            .continueWith { task ->
                task.result!!
                    .documents
                    .mapNotNull { it.toObject(User::class.java) }
                    .filter { u ->
                        u.email != currentUser.email
                                && u.email !in friendEmails
                    }
            }
    }

    fun addOrGetUser(
        unknownUser: User,
    ): Task<User?> {
        val userRef = db.collection(userCollection).document(unknownUser.email)
        return db.runTransaction { transaction ->
            val snap = transaction.get(userRef)
            if (!snap.exists()) {
                transaction.set(userRef, unknownUser)
                unknownUser
            } else {
                snap.toObject(User::class.java)
            }
        }
    }

}