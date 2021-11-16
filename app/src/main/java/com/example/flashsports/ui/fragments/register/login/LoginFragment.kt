package com.example.flashsports.ui.fragments.register.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.databinding.FragmentLoginBinding
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.fragments.register.RegisterFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.hideKeyboard
import com.example.flashsports.utils.isPhoneNumberValid
import com.example.flashsports.utils.snackbar
import com.pixplicity.easyprefs.library.Prefs

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val mViewModel: LoginViewModel by activityViewModels()


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun setupTheme() {
    }

    override fun setupClickListeners() {
        binding.nextBtn.setOnClickListener { checkInputs() }
        binding.forgetPassTv.setOnClickListener {}
        binding.createAccountTv.setOnClickListener { RegisterFragment.updateCurrentItem(1, true) }
    }

    private fun checkInputs() {
        val phoneNumber = binding.etPhoneNumber.editText?.text.toString().trim()
        val error: String? = phoneNumber.isPhoneNumberValid()

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        binding.etPhoneNumber.hideKeyboard()
        navController.navigate(R.id.action_global_enterPinFragment, bundleOf(Config.LOGIN_TYPE to EnterPinType.LOGIN))
    }


}