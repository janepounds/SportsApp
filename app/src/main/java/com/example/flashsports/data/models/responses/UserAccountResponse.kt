package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserAccountResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:UserAccountData?

        )

class UserAccountData(
    @SerializedName("name")
    @Expose
    val name:String,
    @SerializedName("email")
    @Expose
    val email:String,
    @SerializedName("picture")
    @Expose
    val picture:String,


    )

