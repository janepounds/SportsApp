package com.example.flashsports.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.AuthenticationResponse
import com.example.flashsports.data.preferences.UserPreferences
import com.example.flashsports.databinding.FragmentChangePinBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.*
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class ChangePinFragment : BaseFragment<FragmentChangePinBinding>(){
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var pin: String? = null
    private var phoneNumber: String? = null
    private var dialogLoader: DialogLoader? = null
    private val mViewModel: LoanViewModel by activityViewModels()
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentChangePinBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.etOldPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.eetNewPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.etConfirmPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.etOldPin.addEndIconClickListener()
        binding.eetNewPin.addEndIconClickListener()
        binding.etConfirmPin.addEndIconClickListener()

        context?.let {
            mViewModel.getCurrentUser( false, it).observe(viewLifecycleOwner, { user ->
                pin = user.pin
                phoneNumber = user.phoneNumber

            })
        }


    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvRememberMe.setOnClickListener { onRememberMeClick() }
        binding.confirmPinBtn.setOnClickListener { checkInputs() }
        binding.closeBtn.setOnClickListener { requireActivity().onBackPressed() }
    }
    private fun onRememberMeClick(){
        val tag = binding.tvRememberMe.tag.toString()
        if (tag == "remembered") {
            binding.tvRememberMe.tag = "not_remembered"
            binding.tvRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
        } else {
            binding.tvRememberMe.tag = "remembered"
            binding.tvRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_filled, 0, 0, 0)
        }

    }
    private fun checkInputs(){
        val oldPin = binding.etOldPin.editText?.text.toString().trim()
        val newPin = binding.eetNewPin.editText?.text.toString().trim()
        val confirmPin = binding.etConfirmPin.editText?.text.toString().trim()
        val rememberMeValue = binding.tvRememberMe.tag
        var error: String? = null


        if(oldPin!= pin) error = getString(R.string.invalid_pin)
        if (newPin != confirmPin) error = getString(R.string.confirm_pin_not_matches)
        if (newPin.length != 4 || confirmPin.length != 4) error = getString(R.string.invalid_pin)
        if (confirmPin.isEmpty()) error = getString(R.string.confirm_pin_cannot_be_empty)
        if (newPin.isEmpty()) error = getString(R.string.pin_cannot_be_empty)
        if (oldPin.isEmpty()) error = getString(R.string.pin_cannot_be_empty)

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }

        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        /**********************Retrofit to initiate login *********************/
        var call: Call<AuthenticationResponse>? = apiRequests?.changePin(
            phoneNumber,Constants.PREPIN+pin, generateRequestId(),"changeCustomerPin",Constants.PREPIN +newPin,
            Constants.PREPIN+confirmPin
        )
        call!!.enqueue(object : Callback<AuthenticationResponse> {
            override  fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        /***************save user details and login user***************/

                        lifecycleScope.launch {
                            response.body()?.let {
                                Prefs.putString("token",it!!.access_token)
                                Prefs.putString("pin",newPin)
                                Prefs.putString("user_id", it!!.data!!.id.toString())
                                Prefs.putString("name",it!!.data?.name)
                                Prefs.putString("phoneNumber",it!!.data?.phoneNumber)
                                Prefs.putString("email",it!!.data?.email)
                                Prefs.putString("balance",it!!.data?.balance.toString())
                                Prefs.putString("interest_rate",it!!.data?.interest_rate.toString())
                                Prefs.putString("processing_fee",
                                    it!!.data?.processing_fee.toString()
                                )
                                Prefs.putString("payment_due", it!!.data?.payment_due.toString())
                                Prefs.putString("payment_due_date",it!!.data?.payment_due_date)
                                Prefs.putString(
                                    "token_expiry",
                                    it!!.time_expiry
                                )
                                userPreferences.saveIsLoggedIn(true)

                            }
                        }

                    }else{
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                } else if(response.code()==401){
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                }else {
                    if(response.body()!!.message?.isNotEmpty()) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()
                    }
                }

            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })

    }



}