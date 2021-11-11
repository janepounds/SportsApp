package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoanPaymentDetailsResponse(
  @SerializedName("status")
  @Expose
  val status:Int,
  @SerializedName("message")
  @Expose
  val message:String,
  @SerializedName("data")
  @Expose
  val data: LoanPaymentDetailsData?

)

class LoanPaymentDetailsData(
    @SerializedName("totalPayments")
    @Expose
    val totalPayments:Long,
    @SerializedName("amountDue")
    @Expose
    val amountDue:Long
)

