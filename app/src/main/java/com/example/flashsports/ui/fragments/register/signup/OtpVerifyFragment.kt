package com.example.flashsports.ui.fragments.register.signup

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.data.models.RegistrationResponse
import com.example.flashsports.databinding.FragmentOtpVerifyBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.generateRequestId
import com.example.flashsports.utils.snackbar
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class OtpVerifyFragment : BaseFragment<FragmentOtpVerifyBinding>() {

    private val TAG = "OtpVerifyFragment"
    private val mViewModel: LoginViewModel by activityViewModels()
    private var timeLeft: Int = 20
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var loginType: CreatePinType = CreatePinType.SIGNUP


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentOtpVerifyBinding.inflate(
        inflater,
        container,
        false
    )

    override fun setupTheme() {
        loginType = requireArguments().getSerializable(Config.CREATE_PIN_TYPE) as CreatePinType
        binding.isForgotPin = loginType == CreatePinType.FORGOT_PIN
        when (loginType) {
            CreatePinType.FORGOT_PIN -> binding.tvOtpVerify.text = getString(R.string.otp_verify_reset_code)

        }
        binding.phoneNumber = Prefs.getString("phoneNumber")
        binding.timeLeft = timeLeft

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                while (timeLeft > 0) updateTimeLeft()
            }


        }
        binding.etOtp1.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etOtp2.editText?.requestFocus()
                binding.etOtp1.editText?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.slider_bg_with_stroke_9, null
                )

            }

        })
        binding.etOtp2.editText?.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etOtp3.editText?.requestFocus()
                binding.etOtp2.editText?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.slider_bg_with_stroke_9, null
                )

            }
        })

        binding.etOtp3.editText?.addTextChangedListener( object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etOtp4.editText?.requestFocus()
                binding.etOtp3.editText?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.slider_bg_with_stroke_9, null
                )

            }
        })

        binding.etOtp4.editText?.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etOtp4.editText?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.slider_bg_with_stroke_9, null
                )
                checkInputs()
            }
        })

    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvResendCode.setOnClickListener { resendCode() }
        binding.continueBtn.setOnClickListener { checkInputs() }
    }

    private suspend fun updateTimeLeft() {
        delay(1000)
        timeLeft--
        withContext(Dispatchers.Main) {
            binding.timeLeft = timeLeft
        }
    }



    private fun resendCode() {
        if (timeLeft == 0) {
            /***************Retrofit call for resend otp**************************/
            var call: Call<RegistrationResponse>? = apiRequests?.resendOtp(
                Prefs.getString("phoneNumber"),
                "resendUserOTP",
                generateRequestId()

            )
            call!!.enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(
                    call: Call<RegistrationResponse>,
                    response: Response<RegistrationResponse>
                ) {
                    if (response.isSuccessful) {

                        if (response.body()!!.status == 1) {


                        }else{
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                        }

                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    t.message?.let { binding.root.snackbar(it) }

                }
            })



        } else {
            binding.root.snackbar("Please wait. You can resend otp again after $timeLeft seconds.")
        }
    }

    private fun checkInputs() {
        val otp1 = binding.etOtp1.editText?.text.toString().trim()
        val otp2 = binding.etOtp2.editText?.text.toString().trim()
        val otp3 = binding.etOtp3.editText?.text.toString().trim()
        val otp4 = binding.etOtp4.editText?.text.toString().trim()
        var error: String? = null

        if (otp1.isEmpty()) error = getString(R.string.invalid_pin)
        if (otp2.isEmpty()) error = getString(R.string.invalid_pin)
        if (otp3.isEmpty()) error = getString(R.string.invalid_pin)
        if (otp4.isEmpty()) error = getString(R.string.invalid_pin)

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }

        val otpValue = otp1+otp2+otp3+otp4
        mViewModel.setOtp(otpValue)
        when(loginType){
            CreatePinType.FORGOT_PIN ->{
                navController.navigate(R.id.action_otpVerifyFragment_to_passwordResetSuccessFragment,
                    bundleOf(Config.CREATE_PIN_TYPE to CreatePinType.FORGOT_PIN)
                )
            }
            CreatePinType.SIGNUP ->{
                navController.navigate(R.id.action_otpVerifyFragment_to_createPinFragment,
                    bundleOf(Config.CREATE_PIN_TYPE to CreatePinType.SIGNUP)
                )
            }
        }


    }


}