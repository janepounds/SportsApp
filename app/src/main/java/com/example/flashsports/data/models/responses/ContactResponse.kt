package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ContactResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("access_token")
    @Expose
    val access_token: String?,
    @SerializedName("data")
    @Expose
    val data: ContactData?
)
class ContactData(
    @SerializedName("user_id")
    @Expose
    val user_id:Int,
    @SerializedName("district")
    @Expose
    val district: String,
    @SerializedName("village")
    @Expose
    val village: String?,
    @SerializedName("residential_type")
    @Expose
    val residential_type:String?,
    @SerializedName("mobile_phone")
    @Expose
    val mobile_phone: String,
    @SerializedName("landlord")
    @Expose
    val landlord: String?,
    @SerializedName("landlord_contact")
    @Expose
    val landlord_contact: String?,
    @SerializedName("updated_at")
    @Expose
    val updated_at: String?,
    @SerializedName("created_at")
    @Expose
    val created_at: String?,
    @SerializedName("id")
    @Expose
    val id: String?

)