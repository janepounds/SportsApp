package com.example.flashsports.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.models.Loan
import com.example.flashsports.data.models.responses.LoanPaymentDetailsResponse
import com.example.flashsports.databinding.FragmentLoanDetailsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.DialogLoader
import com.example.flashsports.utils.calculation.CalculationUtils
import com.example.flashsports.utils.generateRequestId
import com.example.flashsports.utils.snackbar
import com.example.flashsports.utils.startAuth
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class LoanDetailsFragment : BaseFragment<FragmentLoanDetailsBinding>() {

    private  val TAG = "LoanDetailsFragment"
    private val mViewModel: LoanViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var loan: Loan
    private var weeklyPay:Long? = null
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    private var amountRemaining: Long = 0


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoanDetailsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loan = mViewModel.getLoan()!!
        binding.valueLoanId.text = String.format(getString(R.string.loan_id), loan.loanId)

        getLastRepayment()

        calculateWeeklyPayments(loan.loanDueAmount,loan.durationType,loan.duration)

        binding.tvPaidToAmt.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(loan.paidToDate))
        binding.valueLoanAmt.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(loan.loanDueAmount))
        binding.valueLoanPeriod.text = String.format(getString(R.string.loan_duration)+" "+loan.durationType, loan.duration)
        binding.valueInterestRate.text = String.format(getString(R.string.interest_rate_value), loan.interestRate)
        binding.valueTimeLeft.text = String.format(getString(R.string.loan_duration)+" "+loan.durationType, loan.duration)

    }

    private fun calculateWeeklyPayments(amt: Long, durationType: String, duration: Int) {
        if(durationType.equals("months",ignoreCase = true)){
            weeklyPay = amt /(4 * duration)

            binding.tvWeeklyPaymentValue.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(weeklyPay!!))

        }else if(durationType.equals("weeks",ignoreCase = true)){
            weeklyPay = amt / duration
            binding.tvWeeklyPaymentValue.text = String.format(getString(R.string.wallet_balance_value),MyApplication.getNumberFormattedString(weeklyPay!!))
        }else{
            binding.tvWeeklyPayment.text = getString(R.string.daily_payments)
            weeklyPay = amt/duration
            binding.tvWeeklyPaymentValue.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(
                weeklyPay!!
            ))
        }

    }

    private fun getLastRepayment(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()

        /**********************Retrofit to call get repayments Endpoint *********************/
        var call: Call<LoanPaymentDetailsResponse>? = apiRequests?.getPaymentDetails(
            Prefs.getString("token"),
            loan.loanId,
            generateRequestId(),
            "getLoanPaymentDetails"
        )
        call!!.enqueue(object : Callback<LoanPaymentDetailsResponse> {
            override  fun onResponse(
                call: Call<LoanPaymentDetailsResponse>,
                response: Response<LoanPaymentDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        var valueRemaining = CalculationUtils.calculateLoanBalance(response.body()!!.data!!.totalPayments,response.body()!!.data!!.amountDue)
                        binding.valueRemaining.text = String.format(getString(R.string.wallet_balance_value),MyApplication.getNumberFormattedString(
                            valueRemaining))
                        amountRemaining = valueRemaining

                    }else{
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                } else {  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<LoanPaymentDetailsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })

    }
    override fun setupClickListeners() {
        binding.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.partialBtn.setOnClickListener {
            mViewModel.setAmountToBePaid(amountRemaining)
            navController.navigate(R.id.action_loanDetailsFragment_to_makePaymentsFragment) }
        binding.fullPaymentBtn.setOnClickListener {
            mViewModel.setAmountToBePaid(amountRemaining)
            arguments = Bundle().apply {
                putLong("full_payment", amountRemaining)
            }
            navController.navigate(R.id.action_loanDetailsFragment_to_makePaymentsFragment, arguments) }
    }

}