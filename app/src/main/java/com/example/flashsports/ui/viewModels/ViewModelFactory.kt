package com.example.flashsports.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashsports.data.repositories.UserRepository

object ViewModelFactory {
    class FileViewModelFactory constructor(private val userRepository: UserRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FilesViewModel(userRepository) as T
        }
    }

}