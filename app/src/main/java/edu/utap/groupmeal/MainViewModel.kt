package edu.utap.groupmeal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.groupmeal.model.Invite
import edu.utap.groupmeal.model.Status
import edu.utap.groupmeal.model.User

class MainViewModel : ViewModel() {

    // Track current authentication of user
    private val _currentAuth = MutableLiveData<AuthUserDto>()
    val currentUserAuthorization: LiveData<AuthUserDto>
        get() = _currentAuth

    // Tracks current user
    private var _currentUser: User = User()

    // Database access
    private val userDBHelper = UserDBHelper()

    // Should re-fetch friends
    private val _shouldRefresh = MutableLiveData<Boolean>()
    val updateFriends: LiveData<Boolean>
        get() = _shouldRefresh

    fun addFriend(friend: User, onComplete: (Boolean, Exception?) -> Unit) {
        userDBHelper.addFriend(_currentUser.email, friend.email) { success, e ->
            if (success) {
                onComplete.invoke(true, null)
            } else {
                onComplete.invoke(false, e)
            }
        }
    }

    fun refreshFriends() {
        _shouldRefresh.value = true
    }

    fun getDB(): FirebaseFirestore {
        return userDBHelper.getDB()
    }

    private var lastEmail = ""

    fun getLastUsersWithEmailLike(): Task<List<User>> {
        return userDBHelper.queryUnknownUsers(lastEmail, _currentUser)
    }

    fun getUsersWithEmailLike(email: String): Task<List<User>> {
        lastEmail = email
        return userDBHelper.queryUnknownUsers(email, _currentUser)
    }

    fun getCurrentUserRef(): DocumentReference {
        return userDBHelper.getRefFromEmail(_currentUser.email)
    }

    fun getFriendsDocRef(): List<DocumentReference> = _currentUser.friends

    fun getInviteRef(invite : Invite) : DocumentReference {
        return userDBHelper.getInvite(invite.id)
    }

    fun fetchCurrentFriends(onComplete: (List<DocumentReference>?, Exception?) -> Unit) =
        fetchCurrentUserField({ it.friends }, onComplete)

    fun fetchCurrentInvites(onComplete: (List<DocumentReference>?, Exception?) -> Unit) =
        fetchCurrentUserField({ it.invites }, onComplete)

    fun sendInvite(
        time: Timestamp,
        location: String,
        url: String,
        members: Map<String, Status>,
    ) {
        userDBHelper.sendInvite(time, location, url, members)
    }

    fun  updateInvite(
        inviteId: String,
        response: Status,
        onComplete: (success: Boolean, error: Exception?) -> Unit
    ) {
        userDBHelper.respondToInvite(_currentUser.email, inviteId, response) { success, error ->
            if (success) {
                onComplete.invoke(true, error)
            }
        }
    }

    fun setCurrentAuthUser(user: AuthUserDto) {
        _currentAuth.value = user
        if (_currentAuth.isInitialized) {
            setCurrentUser(user.email, user.name) { _, _ -> }
        }
    }

    fun refresh(onComplete: (success: Boolean, error: Exception?) -> Unit) {
        if (!_currentAuth.isInitialized) {
            onComplete.invoke(false, null)
        } else {
            setCurrentUser(_currentUser.email, _currentUser.name, onComplete)
        }
    }

    private fun setCurrentUser(
        email: String,
        name: String,
        onComplete: (success: Boolean, error: Exception?) -> Unit
    ) {
        userDBHelper.addOrGetUser(
            User(
                name = name,
                email = email
            )
        ).addOnSuccessListener { user ->
            _currentUser = user!!
            onComplete.invoke(true, null)
        }.addOnFailureListener { e ->
            onComplete.invoke(false, e)
        }
    }

    private fun <T> fetchCurrentUserField(
        selector: (User) -> T,
        onComplete: (T?, Exception?) -> Unit
    ) {
        refresh { success, error ->
            if (success) {
                onComplete(selector(_currentUser), null)
            } else {
                onComplete(null, error)
            }
        }
    }


}
