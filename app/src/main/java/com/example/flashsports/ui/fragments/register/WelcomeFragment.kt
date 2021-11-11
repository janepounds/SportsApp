package com.example.flashsports.ui.fragments.register

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.flashsports.R
import com.example.flashsports.databinding.FragmentWelcomeBinding
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun setupTheme() {
    }

    override fun setupClickListeners() {
        binding.signInBtn.setOnClickListener { navigateToRegister(true) }
        binding.createNewAccountTv.setOnClickListener { navigateToRegister(false) }
    }

    private fun navigateToRegister(showLoginFirst: Boolean) {
        navController.navigate(R.id.action_global_registerFragment, bundleOf(RegisterFragment.KEY_SHOW_LOGIN_FIRST to showLoginFirst))
    }


}