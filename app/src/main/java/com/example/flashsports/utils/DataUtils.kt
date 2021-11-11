package com.example.flashsports.utils

import com.example.flashsports.data.repositories.UserRepository
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.network.FileUploader

object DataUtils {

    fun getUserRepo(): UserRepository {
        val userEndPoints = ApiClient.getLoanInstance() as ApiRequests
        return UserRepository.getInstance(
            userEndPoints,
            FileUploader(userEndPoints)
        )
    }

}