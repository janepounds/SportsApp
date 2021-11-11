package com.example.flashsports.data.models

data class User(
    var fullName: String = "",
    var emailAddress: String = "",
    var phoneNumber: String = "",
    var profileImage: String? = "",
    var dateOfBirth: String = "Not set",
    var nin: String = "Not set",
    var regDate: String = "Not set",
    var location: String = "Not set",
    var walletBalance: Double = 0.0,
    var id: Int?=null,
    var accessToken: String?=null,
    val pin: String?=null,
    val interestRate: Float?=null,
    val processingFee: Double?=null,
    var payment_due:Long? = null,
    var payment_due_date:String? = null,

)