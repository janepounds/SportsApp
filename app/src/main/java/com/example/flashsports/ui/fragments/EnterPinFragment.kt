package com.example.flashsports.ui.fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentResolver
import android.net.Uri
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.CreatePinType
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.models.PaymentStatusResponse
import com.example.flashsports.data.models.Sms
import com.example.flashsports.data.models.responses.*
import com.example.flashsports.databinding.DialogLoanStatusBinding
import com.example.flashsports.databinding.DialogPaymentStatusBinding
import com.example.flashsports.databinding.FragmentEnterPinBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.*
import com.example.flashsports.utils.calculation.CalculationUtils
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

@AndroidEntryPoint
class EnterPinFragment : BaseFragment<FragmentEnterPinBinding>() {

    private val mViewModel: LoginViewModel by activityViewModels()
    private val loanViewModel: LoanViewModel by activityViewModels()
    private var loginType: EnterPinType = EnterPinType.LOGIN
    private lateinit var statusDialogBinding: DialogLoanStatusBinding
    private lateinit var checkPaymentStatus: DialogPaymentStatusBinding
    private lateinit var statusDialog: Dialog
    private lateinit var paymentDialog: Dialog
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    private var timeLeft: Int = 40
    private var timeUpdated:String = "1900-01-00 00:00:00"


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentEnterPinBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loginType = requireArguments().getSerializable(Config.LOGIN_TYPE) as EnterPinType
        binding.isLogin = loginType == EnterPinType.LOGIN
        binding.etEnterPin.editText?.transformationMethod = AsteriskPasswordTransformationMethod()
        binding.etEnterPin.editText?.inputType = InputType.TYPE_NULL
        binding.etEnterPin.addEndIconClickListener()
        setupDialog()
        setupPaymentDialog()

        when (loginType) {
            EnterPinType.NEW_LOAN -> binding.title = getString(R.string.authorize_application)
            EnterPinType.WITHDRAW -> binding.title = getString(R.string.withdraw_funds)
            EnterPinType.MAKE_PAYMENT -> binding.title = getString(R.string.authorize_payment)
        }
        /************get user from shared preferences********************/
        if(loginType!=EnterPinType.LOGIN)
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                context?.let {
                    loanViewModel.getCurrentUser(false, it )
                        .observe(viewLifecycleOwner, { user ->
                            user?.let {
                                statusDialogBinding.user = it
                                checkPaymentStatus.user = it

                            }
                        })
                }
            }

        }

    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.closeBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.keypadLayout.deleteBtn.setOnClickListener { onDeleteBtnClick() }
        binding.keypadLayout.doneBtn.setOnClickListener { onDoneBtnClick() }
        binding.keypadLayout.setKeyPadListener { keyValue -> onKeyPadClick(keyValue) }
        binding.clickHereTv.setOnClickListener{  }
    }



    private fun setupDialog() {
        statusDialogBinding = DialogLoanStatusBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        statusDialog = createFullScreenDialog(statusDialogBinding, R.drawable.slider_bg, false)
        when (loginType) {
            EnterPinType.NEW_LOAN -> {
                statusDialogBinding.successImage.visibility = View.GONE
                statusDialogBinding.statusText.text = getString(R.string.loan_review_status_text)
                statusDialogBinding.okayBtn.text = getString(R.string.okay)
            }
            EnterPinType.MAKE_PAYMENT -> {
                statusDialogBinding.successImage.visibility = View.VISIBLE
                statusDialogBinding.statusText.text = String.format(
                    getString(R.string.payment_successfully_text),
                    MyApplication.getNumberFormattedString(loanViewModel.getPayment()?.amount!!),
                    loanViewModel.getLoan()!!.loanId,
                    MyApplication.getNumberFormattedString(CalculationUtils.calculateLoanBalance(loanViewModel.getPayment()?.amount!!,loanViewModel.getLoan()!!.loanDueAmount))
                )
                statusDialogBinding.okayBtn.text = getString(R.string.thank_you)
            }
        }

        statusDialogBinding.okayBtn.setOnClickListener {
            statusDialog.dismiss()

            navController.navigateUsingPopUp(R.id.myLoansFragment, R.id.action_global_homeFragment)
        }

    }

    private fun setupPaymentDialog(){
        checkPaymentStatus = DialogPaymentStatusBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        paymentDialog = createFullScreenDialog(checkPaymentStatus, R.drawable.slider_bg, false)
        checkPaymentStatus.timeLeft = timeLeft

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                while (timeLeft > 0) updateTimeLeft()
            }

        }


    }

    private suspend fun updateTimeLeft() {
        delay(1000)
        timeLeft--
        withContext(Dispatchers.Main) {
            checkPaymentStatus.timeLeft = timeLeft
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onKeyPadClick(keyValue: Int) {
        val pinValue = binding.etEnterPin.editText?.text.toString()
        if (pinValue.length < 4) binding.etEnterPin.editText?.setText("$pinValue$keyValue")
    }

    private fun onDeleteBtnClick() {
        val pinValue = binding.etEnterPin.editText?.text.toString()
        if (pinValue.isNotEmpty()) binding.etEnterPin.editText?.setText(pinValue.dropLast(1))
    }

    private fun onDoneBtnClick() {
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        val pinValue = binding.etEnterPin.editText?.text.toString()
        if (pinValue.length != 4) {
            binding.root.snackbar(getString(R.string.invalid_pin))
            return
        }
        mViewModel.setPin(pinValue)

        when (loginType) {
            EnterPinType.LOGIN -> {
                navController.navigateUsingPopUp(R.id.enterPinFragment, R.id.action_global_homeFragment)

            }
            EnterPinType.NEW_LOAN -> {
                statusDialog.show()

            }

            EnterPinType.WITHDRAW ->{

                navController.navigateUsingPopUp(R.id.welcomeFragment, R.id.action_global_transferredSuccessfullyFragment)

            }

            EnterPinType.MAKE_PAYMENT -> {

                paymentDialog.show()

            }

        }
    }


    fun interface KeyPadListener {
        fun onKeyClick(keyValue: Int)
    }


}

