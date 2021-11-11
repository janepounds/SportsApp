package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoanRepaymentResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data: List<LoanRepaymentData>?
)

class LoanRepaymentData(
    @SerializedName("txnId")
    @Expose
    val txnId:String,
    @SerializedName("txnAmt")
    @Expose
    val txnAmt:Double,
    @SerializedName("txnDate")
    @Expose
    val txnDate:String,
    @SerializedName("txnCurrency")
    @Expose
    val txnCurrency:String,

    )