package com.example.flashsports.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TokenResponse(
    @SerializedName("status")
    @Expose
    val status: Int = 0,
    @SerializedName("message")
    @Expose
    val message: String?,
    @SerializedName("data")
    @Expose
    val data:TokenData

)

class TokenData(
    @SerializedName("access_token")
    @Expose
    val access_token:String?
)