package com.example.flashsports.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RegistrationResponse(
    @SerializedName("status")
    @Expose
    val status: Int,
    @SerializedName("message")
    @Expose
    val message: String?,
    @SerializedName("data")
    @Expose
    val data: ResponseData?

)

class ResponseData(
    @SerializedName("sms_results")
    @Expose
    val sms_results: String?)
