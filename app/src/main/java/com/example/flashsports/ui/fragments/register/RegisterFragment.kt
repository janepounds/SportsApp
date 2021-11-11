package com.example.flashsports.ui.fragments.register

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.flashsports.R
import com.example.flashsports.databinding.FragmentRegisterBinding
import com.example.flashsports.ui.adapters.screen.ViewPagerAdapter
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.fragments.register.login.LoginFragment
import com.example.flashsports.ui.fragments.register.signup.SignUpFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentRegisterBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        val showLoginFirst = requireArguments().getBoolean(KEY_SHOW_LOGIN_FIRST)
        setupViewPager()
        if (!showLoginFirst) updateCurrentItem(1, false)
    }

    override fun setupClickListeners() {

    }

    private fun setupViewPager() {
        val fragments: ArrayList<Fragment> = ArrayList()
        fragments.add(LoginFragment())
        fragments.add(SignUpFragment())

        val adapter = ViewPagerAdapter(this, fragments)
        binding.loginPager.adapter = adapter
        binding.loginPager.offscreenPageLimit = 3

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = getString(R.string.login)
                1 -> tab.text = getString(R.string.sign_up)
            }
        }
        TabLayoutMediator(binding.loginTabs, binding.loginPager, true, tabConfigurationStrategy).attach()
        loginPager = binding.loginPager
    }

    companion object {
        const val KEY_SHOW_LOGIN_FIRST = "show_login_first"
        var loginPager: ViewPager2? = null

        fun updateCurrentItem(forSelectItem: Int, smoothScroll: Boolean) {
            loginPager?.setCurrentItem(forSelectItem, smoothScroll)
        }
    }


}