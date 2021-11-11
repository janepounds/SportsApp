package com.example.flashsports.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.flashsports.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {


    private var otp: String? = null
    private var pin: String? = null

    fun setPin(updated: String){
        pin = updated
    }
    fun getPin() = pin

    fun setOtp(otp_updated:String){
        otp = otp_updated
    }

    fun getOtp() = otp

}