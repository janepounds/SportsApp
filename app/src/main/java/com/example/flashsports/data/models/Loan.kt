package com.example.flashsports.data.models

import com.example.flashsports.data.enums.LoanStatus

data class Loan(
    val loanId: String,
    val status: LoanStatus,
    val amt: Long,
    val interestRate:Double,
    val duration:Int,
    val durationType: String,
    val loanDueAmount:Long,
    val paidToDate:Long

)