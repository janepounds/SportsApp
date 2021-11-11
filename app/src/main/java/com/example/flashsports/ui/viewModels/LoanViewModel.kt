package com.example.flashsports.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashsports.data.models.Loan
import com.example.flashsports.data.models.User
import com.example.flashsports.data.models.Withdraw
import com.example.flashsports.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private var loan: Loan? = null
    var loanAmount: Long = 0
    var loanDueAmount: Long = 0
    var duration: Int = 0
    var type: String = ""
    var typePayment: Long = 0
    var interestRate: Double =0.0
    var processingFee: Double =0.0
    var amtToBePaid: Long =0
    private var withdraw: Withdraw? = null


    fun getCurrentUser(reload: Boolean,context:Context): LiveData<User> = userRepository.getCurrentUser(viewModelScope,  reload,context)

    fun setLoanData(amt: Long, dueAmt: Long, loanDuration: Int, loanDurationType: String, loanTypePayment: Long,loanInterestRate:Double,loanProcessingFee:Double) {
        loanAmount = amt
        loanDueAmount = dueAmt
        duration = loanDuration
        type = loanDurationType
        typePayment = loanTypePayment
        interestRate = loanInterestRate
        processingFee = loanProcessingFee
    }

    fun getLoan() = loan

    fun setLoan(updated: Loan) {
        loan = updated
    }


    fun setWithdraw(updated: Withdraw) {
        withdraw = updated
    }

    fun getWithdraw() = withdraw


    fun setPayment(payment: Withdraw) {
        withdraw = payment
    }

    fun getPayment() = withdraw


    fun setAmountToBePaid(amount:Long){
        amtToBePaid =amount

    }
    fun getAmountToBePaid() = amtToBePaid
}