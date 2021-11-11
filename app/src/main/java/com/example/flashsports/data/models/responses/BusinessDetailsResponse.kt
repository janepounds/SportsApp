package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BusinessDetailsResponse(
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message: String,
    @SerializedName("data")
    @Expose
    val data: BusinessData?
)

class BusinessData(
    @SerializedName("business_name")
    @Expose
    val business_name:String,
    @SerializedName("business_type")
    @Expose
    val business_type: String,
    @SerializedName("selfie_in_business")
    @Expose
    val selfie_in_business:String,
    @SerializedName("reg_date")
    @Expose
    val reg_date: String,
    @SerializedName("reg_no")
    @Expose
    val reg_no:String,
    @SerializedName("location")
    @Expose
    val location: String,
    @SerializedName("contact_person")
    @Expose
    val contact_person:String,
    @SerializedName("phone_number")
    @Expose
    val phone_number: String,
    @SerializedName("no_employees")
    @Expose
    val no_employees:String,
    @SerializedName("avg_monthly_revenue")
    @Expose
    val avg_monthly_revenue: String,
    @SerializedName("industry")
    @Expose
    val industry:String


)