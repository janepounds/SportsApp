package com.example.flashsports.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flashsports.data.repositories.UserRepository
import com.example.flashsports.utils.DialogLoader
import com.example.flashsports.utils.generateRequestId
import kotlin.coroutines.coroutineContext

class FilesViewModel constructor(private val repository: UserRepository) : ViewModel()  {
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<Error>()
    val done = MutableLiveData<Boolean>()

    init {
        isLoading.value = false
        done.value = false
    }


     fun saveSingleDocument(
        loader:DialogLoader,
        file: String,
        type: String,
        onFinish: () -> Unit, onError: (error: String) -> Unit
    ) {
         loader.showProgressDialog()
        isLoading.value = true
        val actionId ="saveUserMediaFile"
        repository.uploadSingleDocument(generateRequestId(), file, type, actionId, {
            isLoading.value = false
            onFinish()
            loader.hideProgressDialog()
        }, {
            onError(it.message ?: "Failed to save data!")
            isLoading.value = false
            error.value = it
            loader.hideProgressDialog()
        })
    }


}