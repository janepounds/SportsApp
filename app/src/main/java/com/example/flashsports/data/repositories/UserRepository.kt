package com.example.flashsports.data.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.flashsports.data.models.User
import com.example.flashsports.data.models.responses.GeneralResponse
import com.example.flashsports.data.preferences.UserPreferences
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.network.FileUploader
import com.example.flashsports.singleton.dataStore
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "User"

@Singleton
class UserRepository @Inject constructor(
    private val endPoints: ApiRequests,
    private val fileUploader: FileUploader
) {
    private var _currentUser: User? = null
    private lateinit var userPreferences: UserPreferences
    private var currentUser: MutableLiveData<User> = MutableLiveData()





    fun getCurrentUser(viewModelScope: CoroutineScope, reload: Boolean,context: Context): MutableLiveData<User> {
        if (_currentUser == null || reload) viewModelScope.launch { loadCurrentUser(context) }
        currentUser.postValue(_currentUser)
        return currentUser
    }



    private suspend fun loadCurrentUser(context: Context) {
        userPreferences = UserPreferences(context.dataStore)


            _currentUser =  User(
                fullName = Prefs.getString("name"),
                emailAddress = Prefs.getString("email"),
                phoneNumber = Prefs.getString("phoneNumber"),
                profileImage = null,
                dateOfBirth = Prefs.getString("dob"),
                nin = Prefs.getString("nin"),
                regDate = Prefs.getString("regDate"),
                location = Prefs.getString("location"),
                walletBalance = Prefs.getString("balance").let { if(it.isNotEmpty()) it.toDouble() else 0.0 },
                payment_due = Prefs.getString("payment_due").let { if(it.isNotEmpty()) it.toLong() else 0 },
                payment_due_date = Prefs.getString("payment_due_date")

                )

            currentUser.postValue(_currentUser)



    }

     fun uploadSingleDocument(
        requestId: String,
        file: String,
        type: String,
        actionId: String,
        onSuccess: (success: GeneralResponse) -> Unit,
        onError: (error: Error) -> Unit
    ) {
        fileUploader.uploadSingleFile(
            requestId, file, type, actionId,
            {
                onSuccess(it)
            }, onError
        )
    }


//    fun getPersonalDetails(viewModelScope: CoroutineScope,context: Context):MutableLiveData<User>{
//       if(_personalDetails!= null) viewModelScope.launch { loadPersonalDetails(context) }
//        personalDetails.postValue(_personalDetails)
//        return personalDetails
//    }
//
//    private suspend fun loadPersonalDetails(context: Context) {
//        userPreferences = UserPreferences(context.dataStore)
//
//        userPreferences.personalInfo?.collect {
//            _currentUser = User(
//
//                nin = it.nin,
//                dateOfBirth = it.dateOfBirth
//
//            )
//
//            currentUser.postValue(_personalDetails)
//        }
//    }

    companion object {

        const val CONNECTION_ERROR = "Connection error!"
        const val NO_RESPONSE = "Unknown response from the server!"

        private var INSTANCE: UserRepository? = null
        fun getInstance(endPoints: ApiRequests, fileUploader: FileUploader): UserRepository {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(endPoints, fileUploader)
            }
            return INSTANCE as UserRepository
        }
    }


}