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


//    private val requestPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//               readSms()
//            } else {
//                Timber.i("Denied")
//            }
//        }

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
        binding.clickHereTv.setOnClickListener{ getOtp() }
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
                /**********************Retrofit to initiate login *********************/
                val call: Call<AuthenticationResponse>? = apiRequests?.initiateLogin(
                    Prefs.getString("phoneNumber"),
                    Constants.PREPIN+pinValue,
                    generateRequestId(),
                    "initiateUserLogin"
                )
                call!!.enqueue(object : Callback<AuthenticationResponse> {
                    override  fun onResponse(
                        call: Call<AuthenticationResponse>,
                        response: Response<AuthenticationResponse>
                    ) {
                        if (response.isSuccessful) {
                            dialogLoader?.hideProgressDialog()
                            if (response.body()!!.status == 1) {
                                /************user_data in shared preferences****************************/

                                lifecycleScope.launch {
                                    response.body()?.let {
                                        Prefs.putString("token", it.access_token)
                                        Prefs.putString("pin", pinValue)
                                        Prefs.putString("user_id", it.data!!.id.toString())
                                        Prefs.putString("name", it.data.name)
                                        Prefs.putString("phoneNumber", it.data.phoneNumber)
                                        Prefs.putString("email", it.data.email)
                                        Prefs.putString("balance", it.data.balance.toString())
                                        Prefs.putString(
                                            "interest_rate",
                                            it.data.interest_rate.toString()
                                        )
                                        Prefs.putString(
                                            "processing_fee",
                                            it.data.processing_fee.toString()
                                        )
                                        Prefs.putString(
                                            "payment_due",
                                            it.data.payment_due.toString()
                                        )
                                        Prefs.putString(
                                            "payment_due_date",
                                            it.data.payment_due_date
                                        )
                                        Prefs.putString(
                                            "token_expiry",
                                            it.time_expiry
                                        )
                                        Prefs.putString(
                                            "business_status",
                                            it.data.business_status.toString()
                                        )

                                        userPreferences.saveIsLoggedIn(true)
                                    }

                                }
                                //save Freshchat data
                                // Get the user object for the current installation

                                //read sms
                                //getMobileMoneyHistory()
                                navController.navigateUsingPopUp(R.id.enterPinFragment, R.id.action_global_homeFragment)


                            }else{
                                response.body()!!.message.let { binding.root.snackbar(it) }
                            }

                        } else {
                            response.body()!!.message.let { binding.root.snackbar(it) }
                        }

                    }

                    override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                        t.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }
                })

            }
            EnterPinType.NEW_LOAN -> {
                /**********************Retrofit to call new loan Endpoint *********************/
                val call: Call<LoanInitiationResponse>? = apiRequests?.postNewLoan(
                    Prefs.getString("token"),
                    loanViewModel.loanAmount.toDouble(),
                    loanViewModel.duration,
                    loanViewModel.type,
                    loanViewModel.loanDueAmount.toDouble(),
                    loanViewModel.interestRate,
                    loanViewModel.processingFee,
                    Constants.PREPIN+pinValue,
                    generateRequestId(),
                    "applyForLoan"
                )
                call!!.enqueue(object : Callback<LoanInitiationResponse> {
                    override  fun onResponse(
                        call: Call<LoanInitiationResponse>,
                        response: Response<LoanInitiationResponse>
                    ) {
                        if (response.isSuccessful) {
                            dialogLoader?.hideProgressDialog()
                            if (response.body()!!.status == 1) {
                                statusDialog.show()
                            }else{
                                dialogLoader?.hideProgressDialog()
                                response.body()!!.message.let { binding.root.snackbar(it) }
                            }

                        }else if(response.code()==401){
                            dialogLoader?.hideProgressDialog()
                            binding.root.snackbar(getString(R.string.session_expired))
                            startAuth(navController)
                        } else {  if(response.body()!!.message.isNotEmpty()) {
                            response.body()!!.message.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                        }

                    }

                    override fun onFailure(call: Call<LoanInitiationResponse>, t: Throwable) {
                        t.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }
                })

            }

            EnterPinType.WITHDRAW ->{
                /**********************Retrofit to call withdraw funds Endpoint *********************/
                val call: Call<WithdrawResponse>? = apiRequests?.withdrawFunds(
                    Prefs.getString("token"),
                    Prefs.getString("phoneNumber"),
                    loanViewModel.getWithdraw()?.amount,
                    Constants.PREPIN+pinValue,
                    getString(R.string.currency_code),
                    generateRequestId(),
                    "mobileMoneyWithdraw"
                )
                call!!.enqueue(object : Callback<WithdrawResponse> {
                    override  fun onResponse(
                        call: Call<WithdrawResponse>,
                        response: Response<WithdrawResponse>
                    ) {
                        if (response.isSuccessful) {
                            dialogLoader?.hideProgressDialog()
                            if (response.body()!!.status == 1) {
                                navController.navigateUsingPopUp(R.id.welcomeFragment, R.id.action_global_transferredSuccessfullyFragment)
                            }else{

                                response.body()!!.message.let { binding.root.snackbar(it) }
                            }

                        }else if(response.code()==401){
                            dialogLoader?.hideProgressDialog()
                            binding.root.snackbar(getString(R.string.session_expired))
                            startAuth(navController)
                        } else {  if(response.body()!!.message.isNotEmpty()) {
                            response.body()!!.message.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                        }

                    }

                    override fun onFailure(call: Call<WithdrawResponse>, t: Throwable) {
                        t.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }
                })

            }

            EnterPinType.MAKE_PAYMENT -> {
                /**********************Retrofit to call make payment Endpoint *********************/
                val call: Call<LoanPaymentResponse>? = apiRequests?.makeLoanPayment(
                    Prefs.getString("token"),
                    loanViewModel.getPayment()?.amount?.toDouble(),
                    loanViewModel.getPayment()?.phoneNumber,
                    Constants.PREPIN+pinValue,
                    getString(R.string.currency_code),
                    loanViewModel.getLoan()!!.loanId,
                    generateRequestId(),
                "mobileMoneyLoanPayment"
                )
                call!!.enqueue(object : Callback<LoanPaymentResponse> {
                    override  fun onResponse(
                        call: Call<LoanPaymentResponse>,
                        response: Response<LoanPaymentResponse>
                    ) {
                        if (response.isSuccessful) {
                            dialogLoader?.hideProgressDialog()
                            if (response.body()!!.status == 1) {
                                //call check payment endpoint
                                    //show processing dialog
                                    paymentDialog.show()
                                    checkPayment(response.body()!!.data?.referenceNumber,statusDialog)


                            }else{
                                response.body()!!.message.let { binding.root.snackbar(it) }
                                navController.navigateUsingPopUp(R.id.enterPinFragment, R.id.action_global_homeFragment)
                            }

                        } else if(response.code()==401){
                            dialogLoader?.hideProgressDialog()
                            binding.root.snackbar(getString(R.string.session_expired))
                            startAuth(navController)
                        }else {  if(response.body()!!.message.isNotEmpty()) {
                            response.body()!!.message.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                        }

                    }

                    override fun onFailure(call: Call<LoanPaymentResponse>, t: Throwable) {
                        t.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }
                })

            }

        }
    }


    fun interface KeyPadListener {
        fun onKeyClick(keyValue: Int)
    }


    private fun getOtp(){
        /*******endpoint for get otp********/
        dialogLoader?.showProgressDialog()
        var call: Call<AuthenticationResponse>? = apiRequests?.forgotPassword(
            Prefs.getString("phoneNumber"),
            generateRequestId(),
            "initiateForgotPin",

            )
        call!!.enqueue(object : Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        navController.navigate(R.id.action_enterPinFragment_to_otpVerifyFragment,
                            bundleOf(Config.CREATE_PIN_TYPE to CreatePinType.FORGOT_PIN))

                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)

                } else
                {  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {

                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })


    }



    private fun checkPayment(referenceNumber: String?, statusDialog: Dialog) {
        val call: Call<PaymentStatusResponse>? = apiRequests?.getPaymentStatus(
            Prefs.getString("token"),
            referenceNumber!!,"checkTransactionStatus", generateRequestId()
        )
        call!!.enqueue(object : Callback<PaymentStatusResponse> {
            override  fun onResponse(
                call: Call<PaymentStatusResponse>,
                response: Response<PaymentStatusResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        //call check payment endpointshow
                        paymentDialog.dismiss()
                        statusDialog.show()
                    }else{
                        paymentDialog.dismiss()
                        response.body()!!.message.let { binding.root.snackbar(it) }
                        navController.navigateUsingPopUp(R.id.myLoansFragment, R.id.action_global_homeFragment)
                    }

                } else if(response.code()==401){
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                }else {  if(response.body()!!.message.isNotEmpty()) {
                    response.body()!!.message.let { binding.root.snackbar(it) }
                }
                }

            }

            override fun onFailure(call: Call<PaymentStatusResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }

            }
        })

    }

//    fun onClickRequestPermission() {
//        when {
//            requireContext().checkSelfPermission(
//                Manifest.permission.READ_SMS
//            ) == PackageManager.PERMISSION_GRANTED -> {
//               readSms()
//            }
//
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                requireActivity(),
//                Manifest.permission.READ_SMS
//            ) -> {
//                binding.root.snackbar(getString(R.string.permission_required))
//                requestPermissionLauncher.launch(
//                    Manifest.permission.READ_SMS
//                )
//            }
//
//            else -> {
//                requestPermissionLauncher.launch(
//                    Manifest.permission.READ_SMS
//                )
//            }
//        }
//    }

    private fun readSms(){
        var amount: String
        var amount2: String
        var amountSent:Long =0
        var amountReceived:Long =0
        val balance: Long  = getMostRecentSms()

        for(i in getAllSms()!!) {
            with(i._address) {
                when {
                    contains(getString(R.string.mtn_mm_address)) || contains(getString(R.string.airtelmoney_address) ) -> {
                        with(i._msg) {
                            when {
                                contains(
                                    "You have received UGX",
                                    ignoreCase = true
                                ) || i._msg.contains(
                                    "Cash deposit of UGX",
                                    ignoreCase = true
                                ) || i._msg.contains(
                                    "You have deposited UGX",
                                    ignoreCase = true
                                ) -> {
                                    try {
                                        amount = i._msg.substring(i._msg.indexOf("UGX") + 3)
                                        amount2 = amount.split(' ')[1]
                                        amountReceived += amount2.replace(",", "").toLong()
                                    }catch (ex: NumberFormatException){

                                    }
                                }
                                contains("You have sent",
                                    ignoreCase = true
                                ) || i._msg.contains("SENT UGX",
                                    ignoreCase = true) -> {
                                    try {
                                        amount = i._msg.substring(i._msg.indexOf("UGX") + 3)
                                        amount2 = amount.split(' ')[1]
                                        amountSent += amount2.replace(",", "").toLong()
                                    }catch (ex: NumberFormatException){

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        Timber.d("received: $amountReceived sent:$amountSent")
        saveMobileMoneyHistory(amountReceived,amountSent,balance)

    }



    private fun getAllSms():  List<Sms>? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateStart = formatter.parse(timeUpdated)

        return  getMessage("date >= ?", arrayOf(dateStart.time.toString()))
    }

    private fun getMessage(filter:String,selectionArgs:Array<String>): List<Sms>?{
        val lstSms: MutableList<Sms> = ArrayList()
        val message = Uri.parse("content://sms/inbox")
        val cr: ContentResolver = requireContext().contentResolver
        val c = cr.query(message, null, filter, selectionArgs, null)
        val totalSMS = c!!.count
        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {

                lstSms.add(
                    Sms(c.getString(c.getColumnIndexOrThrow("_id")), c.getString(
                        c
                            .getColumnIndexOrThrow("address")
                    ),c.getString(c.getColumnIndexOrThrow("body")),c.getString(c.getColumnIndexOrThrow("date")))
                )
                c.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close()
        return lstSms


    }

    private fun getMostRecentSms():  Long {

        val mtnSelection = arrayOf( getString(R.string.mtn_mm_address))
        val airtelSelection = arrayOf( getString(R.string.airtelmoney_address) )
        return getMobileMoneyBalance(mtnSelection) + getMobileMoneyBalance(airtelSelection)
    }

    @SuppressLint("TimberArgCount")
    private fun getMobileMoneyBalance(selectionArgs: Array<String>): Long {
        val cr: ContentResolver = requireContext().contentResolver
        val message = Uri.parse("content://sms/inbox")
        val cursor = cr.query(message, null, "address = ?", selectionArgs, "date DESC LIMIT 1")
        var balance: Long = 0

        if (cursor != null && cursor.moveToFirst()) {

            val body = cursor.getString(cursor.getColumnIndex("body")).replace("[^A-Za-z0-9. ]", "")
            val mmKeys=  resources.getStringArray(R.array.mm_keys)
            mmKeys.sortedArrayWith(StringComparator())
                for (j in mmKeys.indices){
                    val str= mmKeys[j].toString()
                    if(body.contains(str)){
                        try {
                            val subStr=body.substring( body.indexOf( str ) + str.length+1 )
                            balance = subStr.replace(",", "").split('.')[0].split(' ')[0].toLong()

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Timber.e(
                                "NumberFormatException",
                                ex.message.toString() + "***" + body
                            )
                        }
                        //ignore repetitions
                        if(body.contains( getString(R.string.item_key_6) ) && j!=6) break
                        if(body.contains( getString(R.string.item_key_5) ) && j!=5) break
                        if(body.contains( getString(R.string.item_key_4)) && j!=4) break
                    }

                }


        }
        cursor!!.close()
        return balance
    }

//    private fun getMobileMoneyHistory(){
//        val call: Call<MobileMoneyHistoryResponse>? = apiRequests?.getMobMoneyHistory(
//            Prefs.getString("token"),
//            generateRequestId(),"getMobilemoneyHistory"
//        )
//        call!!.enqueue(object : Callback<MobileMoneyHistoryResponse> {
//            override  fun onResponse(
//                call: Call<MobileMoneyHistoryResponse>,
//                response: Response<MobileMoneyHistoryResponse>
//            ) {
//                if (response.isSuccessful) {
//                    if (response.body()!!.status == 1) {
//                        response.body()!!.data.let {
//                           if(it !=null){
//                                timeUpdated = it.updated_at
//                               //read sms between time
//                               onClickRequestPermission()
//                           }else{
//                               //if data is null read all sms
//                               onClickRequestPermission()
//
//                           }
//                        }
//
//                    }else{
//                        response.body()!!.message?.let { binding.root.snackbar(it) }
//
//                    }
//
//                } else if(response.code()==401){
//                    binding.root.snackbar(getString(R.string.session_expired))
//                    startAuth(navController)
//                }else {  if(response.body()!!.message?.isNotEmpty()) {
//                    response.body()!!.message?.let { binding.root.snackbar(it) }
//                }
//                }
//
//            }
//
//            override fun onFailure(call: Call<MobileMoneyHistoryResponse>, t: Throwable) {
//                t.message?.let { binding.root.snackbar(it) }
//
//            }
//        })
//    }

    private fun saveMobileMoneyHistory(amountReceived:Long,amountSent:Long,balance:Long){
        var call: Call<MobileMoneyHistoryResponse>? = apiRequests?.saveMobMoneyHistory(
            Prefs.getString("token"),
            generateRequestId(),"saveMobilemoneyHistory",amountReceived,balance,amountSent
        )
        call!!.enqueue(object : Callback<MobileMoneyHistoryResponse> {
            override  fun onResponse(
                call: Call<MobileMoneyHistoryResponse>,
                response: Response<MobileMoneyHistoryResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        navController.navigateUsingPopUp(R.id.welcomeFragment, R.id.action_global_homeFragment)
                    }else{

                        response.body()!!.message.let { binding.root.snackbar(it) }
                        navController.navigateUsingPopUp(R.id.welcomeFragment, R.id.action_global_homeFragment)
                    }

                } else if(response.code()==401){
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                }else {  if(response.body()!!.message.isNotEmpty()) {
                    response.body()!!.message.let { binding.root.snackbar(it) }
                }
                }

            }

            override fun onFailure(call: Call<MobileMoneyHistoryResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }

            }
        })
    }
    inner class StringComparator: Comparator<String>{
        override fun compare(o1: String, o2: String): Int {
            return o2.length.compareTo(o1.length)
        }
    }

}

