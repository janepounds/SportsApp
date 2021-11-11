package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GuarantorResponse(
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("data")
    @Expose
    val data: GuarantorData?
)

class GuarantorData(
    @SerializedName("name1")
    @Expose
    val name:String,
    @SerializedName("gender1")
    @Expose
    val gender:String,
    @SerializedName("relationship1")
    @Expose
    val relationship: String,
    @SerializedName("mobile_phone1")
    @Expose
    val mobile: String?,
    @SerializedName("residential_address1")
    @Expose
    val residential_address:String,
    @SerializedName("name2")
    @Expose
    val name2:String,
    @SerializedName("gender2")
    @Expose
    val gender2: String,
    @SerializedName("relationship2")
    @Expose
    val relationship2: String?,
    @SerializedName("mobile_phone2")
    @Expose
    val mobile2: String?,
    @SerializedName("residential_address2")
    @Expose
    val residential_address2: String?


)