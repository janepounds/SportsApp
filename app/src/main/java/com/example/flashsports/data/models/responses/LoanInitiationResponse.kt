package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoanInitiationResponse(
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:LoanInitiationResponseData?

)

class LoanInitiationResponseData(
    @SerializedName("user_id")
    @Expose
    val user_id:String,
    @SerializedName("amount")
    @Expose
    val amount:Double,
    @SerializedName("loan_id")
    @Expose
    val loan_id:String,
    @SerializedName("duration")
    @Expose
    val duration:Int,
    @SerializedName("duration_units")
    @Expose
    val duration_units:String,
    @SerializedName("amount_due")
    @Expose
    val amount_due: Double,
    @SerializedName("interest_rate")
    @Expose
    val interest_rate:Double,
    @SerializedName("processing_fee")
    @Expose
    val processing_fee:Double,
    @SerializedName("updated_at")
    @Expose
    val updated_at:String,
    @SerializedName("created_at")
    @Expose
    val created_at:String,
    @SerializedName("id")
    @Expose
    val id:Int,
    @SerializedName("due_date")
    val due_date:String
)
