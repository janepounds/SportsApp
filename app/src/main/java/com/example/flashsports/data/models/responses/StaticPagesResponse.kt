package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class StaticPagesResponse (
        @SerializedName("status")
        @Expose
        val status: Int?,
        @SerializedName("message")
        @Expose
        val message: String?,

        @SerializedName("data")
        @Expose
         val pagesData: PagesDetails?

        )

class PagesDetails(
        @SerializedName("terms_and_conditions")
        @Expose
         val terms_and_conditions:String,
        @SerializedName("loan_agreement")
        @Expose
        val loan_agreement: String?,
        @SerializedName("recovery_policy")
        @Expose
        val recovery_policy: String?

)