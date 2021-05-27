package com.dayaonweb.quoter.view.ui.profile

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dayaonweb.quoter.enums.Status
import com.dayaonweb.quoter.extensions.hideKeyboard
import com.dayaonweb.quoter.service.model.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

private const val TAG = "ProfileViewModel"

class ProfileViewModel : ViewModel() {

    private val _storage = Firebase.storage
    private val _db = Firebase.firestore
    private val _user = MutableLiveData<User>()
    private val _status = MutableLiveData<Status>()
    val user: LiveData<User> = _user
    val status: LiveData<Status> = _status


    fun fetchUserDetails() {
        if (user.value?.id.isNullOrEmpty()) {
            val userId = FirebaseInstallations.getInstance().id
            userId.addOnCompleteListener {
                if (it.isSuccessful)
                    _user.postValue(it.result?.let { id -> User(id, "", "") })
                fetchUserProfilePicture(it.result)
                fetchUserName(it.result)
            }
        }
    }

    fun handleProfileImageResult(result: ActivityResult){
        val resultCode = result.resultCode
        val data = result.data
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data!!
            updateProfileImage(fileUri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Log.e(TAG, "handleProfileImageResult: ${ImagePicker.getError(data)}")
            _status.postValue(Status.UPDATE_FAIL)
        }
    }

    private fun updateProfileImage(fileUri: Uri) {
        // Upload image
        val user = _user.value?:User("","","")
        user.profilePicture = fileUri.toString()
        _user.postValue(user)
    }

    fun updateUserName(userNameInput: TextInputLayout?) {
        if (userNameInput == null)
            return
        val userName = userNameInput.editText?.text?.toString() ?: ""
        if (isValidUsername(userName, userNameInput)) {
            _status.postValue(Status.IN_PROGRESS)
            userNameInput.hideKeyboard()
            userNameInput.clearFocus()
            val data = hashMapOf("name" to userName)
            _user.value?.id?.let { userId ->
                _db.collection("users").document(userId)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        _status.postValue(Status.UPDATE_SUCCESS)
                    }
                    .addOnFailureListener {
                        _status.postValue(Status.UPDATE_FAIL)
                        Log.e(TAG, "updateUserName: ${it.message}", it.cause)
                    }
            }
        }
    }

    private fun isValidUsername(userName: String, userNameInput: TextInputLayout): Boolean {
        if (userName.isEmpty()) {
            userNameInput.error = "Username cannot be empty"
            return false
        }
        if (userName == user.value?.name) {
            userNameInput.error = "Cannot have same user name"
            return false
        }
        userNameInput.error = null
        return true
    }

    private fun fetchUserProfilePicture(userId: String?) {
        if (userId.isNullOrEmpty())
            return
        _storage.reference.child("users/${userId}/DP.jpg").downloadUrl
            .addOnSuccessListener {
                Log.d(TAG, "fetchUserProfilePicture: result=$it")
                val user = _user.value ?: User("", "", "")
                user.profilePicture = it.toString()
                _user.postValue(user)
            }
            .addOnFailureListener {
                Log.e(TAG, "fetchUserProfilePicture: Error is:${it.message}")
            }
    }

    private fun fetchUserName(userId: String?) {
        if (userId.isNullOrEmpty())
            return
        _db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                if (it["name"] == null) {
                    _status.postValue(Status.READ_FAIL)
                    return@addOnSuccessListener
                }
                val name = it["name"] as String
                val user = _user.value ?: User("", "", "")
                user.name = name
                _user.postValue(user)
            }
            .addOnFailureListener {
                _status.postValue(Status.READ_FAIL)
                Log.e(TAG, "fetchUserName: ${it.message}", it.cause)
            }
    }
}