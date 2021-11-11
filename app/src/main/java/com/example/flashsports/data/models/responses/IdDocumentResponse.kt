package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class IdDocumentResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("data")
    @Expose
    val data: IdDocumentData?
)

class IdDocumentData(
    @SerializedName("national_id_front")
    @Expose
    val national_id_front: String,
    @SerializedName("national_id_front_thumb")
    @Expose
    val national_id_front_thumb: String,
    @SerializedName("national_id_back")
    @Expose
    val national_id_back: String,
    @SerializedName("national_id_back_thumb")
    @Expose
    val national_id_back_thumb:String,
    @SerializedName("selfie_in_business")
    @Expose
    val selfie_in_business: String,
    @SerializedName("selfie_in_business_thumb")
    @Expose
    val selfie_in_business_thumb:String,
    @SerializedName("profile_picture")
    @Expose
    val profile_picture: String,
    @SerializedName("profile_picture_thumb")
    @Expose
    val profile_picture_thumb:String

    )
