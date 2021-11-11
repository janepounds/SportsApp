package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BalanceAndPaymentResponse (
        @SerializedName("status")
        @Expose
        val status:Int,
        @SerializedName("message")
        @Expose
        val message:String,
        @SerializedName("data")
        @Expose
        val data: BalancePaymentData

        )

class BalancePaymentData(
        @SerializedName("balance")
        @Expose
        val balance:Double,
        @SerializedName("payment_due_date")
        @Expose
        val nextPaymentDate:String,
        @SerializedName("payment_due")
        @Expose
        val paymentAmountDue:Long,
        @SerializedName("business_status")
        @Expose
        val business_status:Int
)