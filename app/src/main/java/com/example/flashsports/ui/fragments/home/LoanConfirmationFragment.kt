package com.example.flashsports.ui.fragments.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.databinding.FragmentLoanConfirmationBinding
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.*
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class LoanConfirmationFragment : BaseFragment<FragmentLoanConfirmationBinding>() {
    private  val TAG = "LoanConfirmationFragmen"

    private val mViewModel: LoanViewModel by activityViewModels()


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoanConfirmationBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        val type = when (mViewModel.type) {
            getString(R.string.days) -> getString(R.string.daily)
            getString(R.string.weeks) -> getString(R.string.weekly)
            getString(R.string.months) -> getString(R.string.monthly)
            else -> getString(R.string.weekly)
        }.lowercase()

        binding.valueTotalLoan.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(mViewModel.loanDueAmount))
        binding.tvRepaymentTime.text = String.format(getString(R.string.repayment_time_value), mViewModel.duration, mViewModel.type)
        binding.tvStartingDate.text = String.format(getString(R.string.starting_date), getLoanRepaymentStartDate(mViewModel.type))
        binding.tvLoanText.text = spannedFromHtml(
            getPayBackText(
                payBackAmount = MyApplication.getNumberFormattedString(mViewModel.loanDueAmount),
                type = type,
                typeAmount = MyApplication.getNumberFormattedString(mViewModel.typePayment),
                interestRate = mViewModel.interestRate
            )
        )

        var loanPeriod = mViewModel.duration.toString()+" "+mViewModel.type
        var agreementDescription = Constants.USER_LOAN_AGREEMENT!!
        var agreement1 = agreementDescription!!.replace( Config.LOAN_AMOUNT,"BORROWER a loan of UGX <b>${MyApplication.getNumberFormattedString(mViewModel.loanAmount)} </b>")
        var agreement2 = agreement1.replace(Config.LOAN_PERIOD," period of <b>$loanPeriod</b>  ." )
        var agreement3 = agreement2.replace(Config.LOAN_PURPOSE, "<b>"+getString(R.string.loan_agreement_reason)+"</b>" )
        var agreement4 = agreement3.replace(Config.LOAN_STARTING_DATE," <b>${binding.tvStartingDate.text}</b>" )
        Constants.USER_LOAN_AGREEMENT = agreement4


    }

    override fun setupClickListeners() {
        binding.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.confirmBtn.setOnClickListener { checkInputs() }

    }


    private fun checkInputs() {
        val agreementChecked = binding.agreementCheckbox.isChecked
        if (!agreementChecked) {
            binding.root.snackbar("Please accept the loan disclosure, loan agreement and recovery policy.")
            return
        }
        navController.navigate(R.id.action_global_enterPinFragment, bundleOf(Config.LOGIN_TYPE to EnterPinType.NEW_LOAN))
    }

}