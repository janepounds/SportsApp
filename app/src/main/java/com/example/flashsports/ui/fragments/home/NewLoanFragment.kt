package com.example.flashsports.ui.fragments.home

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.enums.LoanStatus
import com.example.flashsports.data.models.Loan
import com.example.flashsports.data.models.Sms
import com.example.flashsports.data.models.responses.LoanData
import com.example.flashsports.data.models.responses.LoanResponse
import com.example.flashsports.databinding.DialogLoanStatusBinding
import com.example.flashsports.databinding.FragmentNewLoanBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.*
import com.example.flashsports.utils.calculation.CalculationUtils
import com.example.flashsports.utils.calculation.CalculationUtils.Companion.calculateProcessingFee
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

@AndroidEntryPoint
class NewLoanFragment : BaseFragment<FragmentNewLoanBinding>() {

    private val mViewModel: LoanViewModel by activityViewModels()
    private var interestRate:Double? =0.0
    private var processingFee:Double? =0.0
    private lateinit var statusDialogBinding: DialogLoanStatusBinding
    private lateinit var statusDialog: Dialog
    private lateinit var loanDialog: Dialog
    private lateinit var loanStatusDialog: Dialog
    private val loanViewModel: LoanViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private lateinit var list: ArrayList<LoanData>




    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentNewLoanBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.spinnerDuration.initSpinner(this)
        processingFee = Prefs.getString("processing_fee").toDouble()
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                context?.let {
                    loanViewModel.getCurrentUser(false, it )
                        .observe(viewLifecycleOwner, { user ->
                            user?.let {
                                statusDialogBinding.user = it

                            }
                        })
                }
            }

        }
        setupDialog()
        setUpLoanDialog()
        setupLoanStatusDialog()
        getLoanHistory()

    }

    override fun setupClickListeners() {
        binding.enterBtn.setOnClickListener { checkInputs() }
        binding.applyBtn.setOnClickListener { applyLoan() }
    }

    private fun checkInputs() {
        val durationTypeArray: List<String> = listOf(*resources.getStringArray(R.array.duration))

        val amount = binding.amtEditText.text.toString().trim()
        val duration = binding.durationEditText.text.toString().trim()
        var durationType: String? = null
        var error: String? = null
        if (duration.toInt() > 8) {
            binding.root.snackbar(getString(R.string.max_loan_duration))

        } else {

            if (binding.spinnerDuration.selectedIndex >= 0) {
                durationType = durationTypeArray[binding.spinnerDuration.selectedIndex]
            } else {
                error = String.format(
                    getString(R.string.select_error),
                    getString(R.string.duration_type)
                )
            }
            if (duration.isEmpty()) error = String.format(
                getString(R.string.cannot_be_empty_error),
                getString(R.string.duration)
            )
            if (amount.isEmpty()) error =
                String.format(getString(R.string.cannot_be_empty_error), getString(R.string.amount))

            if (!error.isNullOrEmpty()) {
                binding.root.snackbar(error)
                return
            }
            interestRate = when (durationType) {
                "WKS" -> 2.5
                else -> Prefs.getString("interest_rate").toDouble()
            }

            val loanDueAmount = CalculationUtils.calculateLoanDueAmount(
                amount.toLong(),
                interestRate!!,
                calculateProcessingFee(processingFee!!, amount.toLong()),
                duration.toInt()
            )
            calculateInputs(
                amount.toLong(),
                loanDueAmount.toLong(),
                interestRate!!,
                calculateProcessingFee(processingFee!!, amount.toLong()),
                duration.toInt(),
                durationType!!
            )
        }
    }

    private fun calculateInputs(loanAmount: Long, loanDueAmount: Long, interestRate: Double, processingFee: Double, duration: Int, durationType: String) {
        val payment: Long = loanDueAmount / duration
        val type = when (durationType) {
            "WKS" -> getString(R.string.weeks)
            "MTHS" -> getString(R.string.months)
            else -> getString(R.string.weeks)
        }
        binding.valueDueAmt.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(loanDueAmount))
        binding.valueInterestRate.text = String.format(getString(R.string.interest_rate_value), interestRate)
        binding.valueProcessingFee.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(processingFee.toLong()))
        binding.valueDuration.text = String.format(getString(R.string.duration_with_type_value), duration, type)
        binding.valuePayment.text = String.format(getString(R.string.ugx_with_duration), MyApplication.getNumberFormattedString(payment), type.lowercase().substring(0, type.length - 1))

        mViewModel.setLoanData(loanAmount, loanDueAmount, duration, type, payment,interestRate,processingFee)
    }

    private fun applyLoan() {
        if (binding.valueDueAmt.text.toString().isEmpty()) {
            binding.root.snackbar("Please click on enter and then you can apply for loan!")
            return
        }
        if(binding.amtEditText.text.toString().toLong() <= Prefs.getString("max_loan_amount").toLong()){
            if(Prefs.getString("business_status").toInt() == 0){
                statusDialog.show()
            }else {
                list = ArrayList()
                if(list.size > 0){
                for(i in list){
                    when(i.status){
                        "Pending" -> loanDialog.show()
                        "Approved"-> loanStatusDialog.show()
                        else-> navController.navigate(R.id.action_global_loanConfirmationFragment)

                    }

                }
            }else{
                    navController.navigate(R.id.action_global_loanConfirmationFragment)

                }
            }
        }else{
            binding.root.snackbar("You are not eligible for this loan amount")
        }

    }
    private fun getLoanHistory() {
        /**********************Retrofit to call loan history Endpoint *********************/
        var call: Call<LoanResponse>? = apiRequests?.getLoanHistory(
            Prefs.getString("token"),
            generateRequestId(),
            "getLoanHistory"
        )
        call!!.enqueue(object : Callback<LoanResponse> {
            override  fun onResponse(
                call: Call<LoanResponse>,
                response: Response<LoanResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        for (i in response.body()!!.data!!){
                            list = ArrayList()
                            list.add(LoanData(i.loan_id,i.amount,i.duration,i.duration_units,i.amount_due,i.interest_rate,i.status,i.approved_at,i.processing_fee,i.payment,i.due_date,i.paid_to_date))
                        }

                    }else{

                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                } else{  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }

                }
                }

            }

            override fun onFailure(call: Call<LoanResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }


            }
        })

    }



    private fun setupDialog() {
        statusDialogBinding = DialogLoanStatusBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        statusDialog = createFullScreenDialog(statusDialogBinding, R.drawable.slider_bg, false)
        statusDialogBinding.successImage.visibility = View.GONE
        statusDialogBinding.statusText.text = getString(R.string.business_status_text)
        statusDialogBinding.okayBtn.text = getString(R.string.okay)
        statusDialogBinding.okayBtn.setOnClickListener {
            statusDialog.dismiss()
        }

    }

    private fun setUpLoanDialog(){
        statusDialogBinding = DialogLoanStatusBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        loanDialog = createFullScreenDialog(statusDialogBinding, R.drawable.slider_bg, false)
        statusDialogBinding.successImage.visibility = View.GONE
        statusDialogBinding.statusText.text = getString(R.string.loan_pending_text)
        statusDialogBinding.okayBtn.text = getString(R.string.okay)
        statusDialogBinding.okayBtn.setOnClickListener {
            loanDialog.dismiss()
        }
    }

    private fun setupLoanStatusDialog(){
        statusDialogBinding = DialogLoanStatusBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        loanStatusDialog = createFullScreenDialog(statusDialogBinding, R.drawable.slider_bg, false)
        statusDialogBinding.successImage.visibility = View.GONE
        statusDialogBinding.statusText.text = getString(R.string.loan_ongoing_text)
        statusDialogBinding.okayBtn.text = getString(R.string.okay)
        statusDialogBinding.okayBtn.setOnClickListener {
            loanStatusDialog.dismiss()
        }
    }

}