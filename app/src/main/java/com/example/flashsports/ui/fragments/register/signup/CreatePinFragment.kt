package com.example.flashsports.ui.fragments.register.signup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.data.models.responses.AuthenticationResponse
import com.example.flashsports.databinding.FragmentCreatePinBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.fragments.register.RegisterFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.*

import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class CreatePinFragment : BaseFragment<FragmentCreatePinBinding>() {

    private val TAG = "CreatePinFragment"
    private val mViewModel: LoginViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    private var loginType: CreatePinType = CreatePinType.SIGNUP



    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentCreatePinBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loginType = requireArguments().getSerializable(Config.CREATE_PIN_TYPE) as CreatePinType
        binding.etPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.etConfirmPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.etPin.addEndIconClickListener()
        binding.etConfirmPin.addEndIconClickListener()
    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvRememberMe.setOnClickListener { onRememberMeClick() }
        binding.confirmPinBtn.setOnClickListener { checkInputs() }
        binding.signInTv.setOnClickListener {
            navController.navigateUsingPopUp(R.id.registerFragment, R.id.action_global_registerFragment, bundleOf(RegisterFragment.KEY_SHOW_LOGIN_FIRST to true))
        }

    }

    private fun onRememberMeClick() {
        val tag = binding.tvRememberMe.tag.toString()
        if (tag == "remembered") {
            binding.tvRememberMe.tag = "not_remembered"
            binding.tvRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
        } else {
            binding.tvRememberMe.tag = "remembered"
            binding.tvRememberMe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_filled, 0, 0, 0)
        }
    }

    private fun checkInputs() {
        dialogLoader = context?.let { DialogLoader(it) }

        val pin = binding.etPin.editText?.text.toString().trim()
        val confirmPin = binding.etConfirmPin.editText?.text.toString().trim()
        val rememberMeValue = binding.tvRememberMe.tag
        var error: String? = null

        if (pin != confirmPin) error = getString(R.string.confirm_pin_not_matches)
        if (pin.length != 4 || confirmPin.length != 4) error = getString(R.string.invalid_pin)
        if (confirmPin.isEmpty()) error = getString(R.string.confirm_pin_cannot_be_empty)
        if (pin.isEmpty()) error = getString(R.string.pin_cannot_be_empty)

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }

        when (loginType) {
            CreatePinType.SIGNUP -> {
                /**********navigate to home fragment**************/
                navController.navigateUsingPopUp(
                    R.id.welcomeFragment,
                    R.id.action_global_homeFragment
                )

            }
            CreatePinType.FORGOT_PIN -> {


            }
        }
    }


}

