package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MobileMoneyHistoryResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:MobileMoneyData?
        )

class MobileMoneyData(
    @SerializedName("amount_received")
    @Expose
    val amount_received:Long,
    @SerializedName("amount_sent")
    @Expose
    val amount_sent:Long,
    @SerializedName("updated_at")
    @Expose
    val updated_at:String
)