package com.example.flashsports.ui.fragments.register.signup

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.data.models.RegistrationResponse
import com.example.flashsports.databinding.FragmentSignUpBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.fragments.register.RegisterFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.DialogLoader
import com.example.flashsports.utils.generateRequestId
import com.example.flashsports.utils.isPhoneNumberValid
import com.example.flashsports.utils.snackbar
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {

    private val mViewModel: LoginViewModel by activityViewModels()
//    private var apiRequests: ApiRequests = getIn

    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSignUpBinding.inflate(
            inflater,
            container,
            false
        )

    override fun setupTheme() {

    }

    override fun setupClickListeners() {
        binding.createAccountBtn.setOnClickListener { checkInputs() }
        binding.signInTv.setOnClickListener { RegisterFragment.updateCurrentItem(0, true) }
    }

    private fun checkInputs() {
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        val fullName = binding.etFullName.editText?.text.toString().trim()
        val emailAddress = binding.etEmailAddress.editText?.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.editText?.text.toString().trim()
        var error: String? = phoneNumber.isPhoneNumberValid()

        if (!binding.termsAndConditionCheckbox.isChecked) error =
            getString(R.string.tac_not_accepted)
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) error =
            getString(R.string.invalid_email)
        if (emailAddress.isEmpty()) error = getString(R.string.email_address_cannot_be_empty)
        if (fullName.isEmpty()) error = getString(R.string.full_name_cannot_be_empty)

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        Prefs.putString("phoneNumber",getString(R.string.phone_code)+phoneNumber)


        /*****************Retrofit call for sending otp******************************/
        var call: Call<RegistrationResponse>? = apiRequests?.signUp(
            generateRequestId(),
            "registerUser",
            fullName,
            getString(R.string.phone_code)+ phoneNumber,
            emailAddress
        )
        call!!.enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(
                call: Call<RegistrationResponse>,
                response: Response<RegistrationResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {


                        /**********navigate to otp fragment**************/
                        navController.navigate(R.id.action_registerFragment_to_otpVerifyFragment, bundleOf(
                            Config.CREATE_PIN_TYPE to CreatePinType.SIGNUP))
                    }else{
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                } else {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                }

            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })


    }


}





