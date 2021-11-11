package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VerificationDocumentsResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data: VerificationDocumentsData? = null
)

class VerificationDocumentsData(
    @SerializedName("business_photo")
    @Expose
    val business_photo:String,
    @SerializedName("business_photo_thumb")
    @Expose
    val business_photo_thumb:String,
    @SerializedName("selfie_in_business")
    @Expose
    val selfie_in_business:String,
    @SerializedName("selfie_in_business_thumb")
    @Expose
    val selfie_in_business_thumb:String,
    @SerializedName("neighbourhood_photo")
    @Expose
    val neighbourhood_photo:String,
    @SerializedName("neighbourhood_photo_thumb")
    @Expose
    val neighbourhood_photo_thumb:String,
    @SerializedName("utility_bill")
    @Expose
    val utility_bill:String,
    @SerializedName("utility_bill_thumb")
    @Expose
    val utility_bill_thumb:String,
    @SerializedName("business_video")
    @Expose
    val business_video:String,
    @SerializedName("business_video_thumb")
    @Expose
    val business_video_thumb:String
)