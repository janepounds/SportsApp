package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoanPaymentResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data: LoanPaymentData?
        )

class LoanPaymentData(
    @SerializedName("referenceNumber")
    @Expose
    val referenceNumber:String
)