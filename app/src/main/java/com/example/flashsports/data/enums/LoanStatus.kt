package com.example.flashsports.data.enums

enum class LoanStatus(val status: String) {
    APPROVED("Approved"),
    PAID("Paid"),
    PENDING("Pending"),
    PARTIALLY_PAID("Partially Paid"),
    REJECTED("Rejected")
}