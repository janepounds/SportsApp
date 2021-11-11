package com.example.flashsports.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.flashsports.R
import com.example.flashsports.databinding.FragmentTransferredSuccessfullyBinding
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.navigateUsingPopUp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferredSuccessfullyFragment : BaseFragment<FragmentTransferredSuccessfullyBinding>() {

    private val mViewModel: LoanViewModel by activityViewModels()


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentTransferredSuccessfullyBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        /************get user from shared preferences********************/

        context?.let {
            mViewModel.getCurrentUser( false, it).observe(viewLifecycleOwner, { user ->
                user?.let {
                    binding.user = it
                }
            })
        }


    }

    override fun setupClickListeners() {
        binding.viewDetailsBtn.setOnClickListener {

        }
        binding.continueBtn.setOnClickListener {
            navController.navigateUsingPopUp(R.id.withdrawFundsFragment, R.id.action_global_homeFragment)
        }
    }

}