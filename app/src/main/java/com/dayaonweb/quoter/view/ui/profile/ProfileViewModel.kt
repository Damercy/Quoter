package com.dayaonweb.quoter.view.ui.profile

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.enums.Status
import com.dayaonweb.quoter.extensions.hideKeyboard
import com.dayaonweb.quoter.extensions.readFromPreferences
import com.dayaonweb.quoter.service.model.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

private const val TAG = "ProfileViewModel"

class ProfileViewModel : ViewModel() {

    private val _storage = Firebase.storage
    private val _db = Firebase.firestore
    private val _user = MutableLiveData<User>()
    private val _status = MutableLiveData<Status>()
    private val _progressAmount = MutableLiveData<Int>()
    val user: LiveData<User> = _user
    val status: LiveData<Status> = _status
    val progressAmount: LiveData<Int> = _progressAmount


    fun fetchUserDetails(context: Context) {
        if (_user.value != null)
            return
        viewModelScope.launch {
            val userId = context.readFromPreferences(Constants.USER_ID) ?: ""
            if (userId.isNotEmpty()) {
                Log.d(TAG, "fetchUserDetails: uservalue=${_user.value}")
                _user.postValue(User(userId, "", ""))
                fetchUserProfilePicture(userId)
                fetchUserName(userId)
            }
        }
    }

    fun handleProfileImageResult(result: ActivityResult) {
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
        val user = _user.value ?: User("", "", "")
        user.profilePicture = fileUri.toString()
        _user.postValue(user)
        uploadProfilePicture(fileUri)
    }

    private fun uploadProfilePicture(profilePictureUri: Uri) {
        val userId = _user.value?.id
        if (userId.isNullOrEmpty()) {
            _status.postValue(Status.UPDATE_FAIL)
            return
        }
        val fileNameWithExt =
            if (profilePictureUri.lastPathSegment != null && profilePictureUri.lastPathSegment?.contains(
                    "."
                ) == true
            ) profilePictureUri.lastPathSegment else "DP.jpg"
        Log.d(TAG, "uploadProfilePicture: lastPathSegment=${fileNameWithExt}")
        _storage.reference.child(
            "users/$userId/profilePicture/DP${
                fileNameWithExt?.substring(
                    fileNameWithExt.lastIndexOf(".")
                )
            }"
        )
            .putFile(profilePictureUri)
            .addOnSuccessListener {
                _status.postValue(Status.UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                Log.e(TAG, "uploadProfilePicture: ${it.message}", it.cause)
                _status.postValue(Status.UPDATE_FAIL)
            }
            .addOnProgressListener { progress ->
                _status.postValue(Status.IN_PROGRESS)
                val progressAmount = (progress.bytesTransferred / progress.totalByteCount) * 100
                _progressAmount.postValue(progressAmount.toInt())
            }
    }

    fun updateUserName(userNameInput: TextInputLayout?) {
        if (userNameInput == null)
            return
        val userName = userNameInput.editText?.text?.toString() ?: ""
        if (isValidUsername(userName, userNameInput)) {
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
        _storage.reference.child("users/$userId/profilePicture")
            .listAll()
            .addOnSuccessListener { files ->
                if (files.items.size > 0) {
                    val profilePicture = files.items[0]
                    profilePicture.downloadUrl
                        .addOnSuccessListener {
                            val user = _user.value ?: User("", "", "")
                            Log.d(TAG, "fetchUserProfilePicture: user=$user")
                            user.profilePicture = it.toString()
                            _user.postValue(user)
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "fetchUserProfilePicture: Error is:${it.message}")
                        }
                }
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