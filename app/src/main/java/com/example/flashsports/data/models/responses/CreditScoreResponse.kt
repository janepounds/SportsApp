package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CreditScoreResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:CreditScoreData?

)
class CreditScoreData(
    @SerializedName("credit_score")
    @Expose
    val credit_score:Float
)
