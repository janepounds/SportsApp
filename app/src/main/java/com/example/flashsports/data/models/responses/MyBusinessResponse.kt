package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MyBusinessResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data: MyBusinessData
        )

class MyBusinessData(
    @SerializedName("dob")
    @Expose
    val dob:String,
    @SerializedName("nin")
    @Expose
    val nin:String,
    @SerializedName("business_name")
    @Expose
    val business_name:String,
    @SerializedName("location")
    @Expose
    val location:String,
    @SerializedName("reg_date")
    @Expose
    val regDate:String,
    @SerializedName("trade_license")
    @Expose
    val tradeLincense:String,
    @SerializedName("reg_certificate")
    @Expose
    val regCert:String,
    @SerializedName("tax_reg_certificate")
    @Expose
    val taxCert:String
)