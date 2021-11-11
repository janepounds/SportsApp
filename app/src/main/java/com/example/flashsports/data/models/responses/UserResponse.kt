package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String?,
    @SerializedName("data")
    @Expose
    val data:DataResponse

)

class DataResponse(
    @SerializedName("name")
    @Expose
    val name:String?,
    @SerializedName("dob")
    @Expose
    val dob:String?,
    @SerializedName("nin")
    @Expose
    val nin:String?,
    @SerializedName("gender")
    @Expose
    val gender:String?,
    @SerializedName("marital_status")
    @Expose
    val marital_status:String?,
    @SerializedName("education_level")
    @Expose
    val education_level:String?,
    @SerializedName("years_in_business")
    @Expose
    val years_in_business:Int?,
    @SerializedName("no_of_dependants")
    @Expose
    val no_of_dependents:Int
)
