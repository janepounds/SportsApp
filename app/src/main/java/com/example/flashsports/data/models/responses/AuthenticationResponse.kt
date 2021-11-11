package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AuthenticationResponse(
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("access_token")
    @Expose
    val access_token: String?,
    @SerializedName("time_expiry")
    @Expose
    val time_expiry:String,
    @SerializedName("data")
    @Expose
    val data: UserData?


)

    class UserData(

        @SerializedName("id")
        @Expose
        val id:Int,
        @SerializedName("name")
        @Expose
        val name: String,
        @SerializedName("email")
        @Expose
        val email: String,
        @SerializedName("phoneNumber")
        @Expose
        val phoneNumber: String,
        @SerializedName("balance")
        @Expose
        var balance: Double,
        @SerializedName("interest_rate")
        @Expose
        var interest_rate: Float,
        @SerializedName("processing_fee")
        @Expose
        var processing_fee: Double,

        @SerializedName("dob")
        @Expose
        var dob: String,
        @SerializedName("nin")
        @Expose
        var nin: String,
        @SerializedName("payment_due_date")
        @Expose
        val payment_due_date:String,
        @SerializedName("payment_due")
        val payment_due:Long,
        @SerializedName("business_status")
        @Expose
        val business_status:Int
)

