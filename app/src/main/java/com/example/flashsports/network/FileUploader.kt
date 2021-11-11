package com.example.flashsports.network

import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.GeneralResponse
import com.example.flashsports.data.repositories.UserRepository
import com.pixplicity.easyprefs.library.Prefs
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File



class FileUploader constructor(private var fileAPIEndPoints: ApiRequests) {

    private var index = 0

    fun with(fileAPIEndPoints: ApiRequests): FileUploader {
        return this
    }

    private fun getBodyPart(label: String, path: String): MultipartBody.Part {
        val file = File(path)
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(label, file.name, requestFile)
        // MultipartBody.Part is used to send also the actual file name
    }



    fun uploadSingleFile(
        request_id: String,
        file: String,
        type: String,
        action_id: String,
        onSuccess: (docs: GeneralResponse) -> Unit,
        onError: (error: Error) -> Unit
    ) {
        fileAPIEndPoints.uploadDocument(
            Prefs.getString("token"),
            request_id,
            type, action_id,
            getBodyPart("media_file", file)
        ).enqueue(object : Callback<GeneralResponse> {

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                onError(Error(UserRepository.CONNECTION_ERROR))
            }

            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                response.body()?.let {
                    if (it.status!=0) {
                        onSuccess(it)
                    } else {
                        onError(Error(it.message))
                    }
                }
                if (response.body() == null) {
                    onError(Error(UserRepository.NO_RESPONSE))
                }
            }
        })
    }

    companion object {
        private var INSTANCE: FileUploader? = null
        fun with(fileAPIEndPoints: ApiRequests): FileUploader {
            if (INSTANCE == null) {
                INSTANCE = FileUploader(fileAPIEndPoints)
            } else {
                INSTANCE!!.fileAPIEndPoints = fileAPIEndPoints
            }
            return INSTANCE as FileUploader
        }
    }

}