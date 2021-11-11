package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoanResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:List<LoanData>?
        )

class LoanData(
    @SerializedName("loan_id")
    @Expose
    val loan_id:String,
    @SerializedName("amount")
    @Expose
    val amount:Double,
    @SerializedName("duration")
    @Expose
    val duration:Int,
    @SerializedName("duration_units")
    @Expose
    val duration_units:String?,
    @SerializedName("amount_due")
    @Expose
    val amount_due:Double,
    @SerializedName("interest_rate")
    @Expose
    val interest_rate:Double,
    @SerializedName("status")
    @Expose
    val status:String,
    @SerializedName("approved_at")
    @Expose
    val approved_at:String?,
    @SerializedName("processing_fee")
    @Expose
    val processing_fee:Double,
    @SerializedName("payment")
    @Expose
    val payment:Double,
    @SerializedName("due_date")
    @Expose
    val due_date:String,
    @SerializedName("paid_to_date")
    @Expose
    val paid_to_date:Long


)
