package com.example.flashsports.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.databinding.FragmentPasswordResetSuccessBinding
import com.example.flashsports.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordResetSuccessFragment : BaseFragment<FragmentPasswordResetSuccessBinding>()  {
    private var loginType: CreatePinType = CreatePinType.FORGOT_PIN

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPasswordResetSuccessBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loginType = requireArguments().getSerializable(Config.CREATE_PIN_TYPE) as CreatePinType
        binding.isForgotPin = loginType == CreatePinType.FORGOT_PIN

    }

    override fun setupClickListeners() {
        binding.tvCreatePin.setOnClickListener{navController.navigate(R.id.action_passwordResetSuccessFragment_to_createPinFragment, bundleOf(Config.CREATE_PIN_TYPE to CreatePinType.FORGOT_PIN))}

    }

}