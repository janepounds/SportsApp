package com.example.flashsports.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PaymentStatusResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:PaymentData?

        )

class PaymentData(

)