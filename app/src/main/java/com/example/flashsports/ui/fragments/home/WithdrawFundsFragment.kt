package com.example.flashsports.ui.fragments.home

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.models.Transaction
import com.example.flashsports.data.models.Withdraw
import com.example.flashsports.data.models.responses.LoanRepaymentResponse
import com.example.flashsports.data.models.responses.WithdrawResponse
import com.example.flashsports.databinding.DialogWithdrawFundsConfirmationBinding
import com.example.flashsports.databinding.FragmentWithdrawFundsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.adapters.TransactionAdapter
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.*
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class WithdrawFundsFragment : BaseFragment<FragmentWithdrawFundsBinding>() {

    private val mViewModel: LoanViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var adapter: TransactionAdapter
    private lateinit var withdrawConfirmationDialogBinding: DialogWithdrawFundsConfirmationBinding
    private lateinit var withdrawFundsDialog: Dialog
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentWithdrawFundsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        setupDialog()
        binding.tvBalance.text = String.format(getString(R.string.balance_amt), Prefs.getString("balance"))
        getPastWithdraw()
    }

    private fun getPastWithdraw(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()

        /**********************Retrofit to call withdraw funds Endpoint *********************/
        var call: Call<LoanRepaymentResponse>? = apiRequests?.getPastWithdraw(
            Prefs.getString("token"),
            generateRequestId(),
            "getWithdrawHistory"
        )
        call!!.enqueue(object : Callback<LoanRepaymentResponse> {
            override  fun onResponse(
                call: Call<LoanRepaymentResponse>,
                response: Response<LoanRepaymentResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        adapter = TransactionAdapter()
                        binding.recyclerView.adapter = adapter
                        val tempList = ArrayList<Transaction>()
                        for (i in response.body()!!.data!!){
                            tempList.add(Transaction(txnId = i.txnId, txnAmt =i.txnAmt.toLong(), txnDate = i.txnDate))
                        }
                        adapter.updateList(tempList)

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

            override fun onFailure(call: Call<LoanRepaymentResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })


    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.withdrawBtn.setOnClickListener { checkInputs() }
        binding.tvViewMore.setOnClickListener { loadMoreTransactions() }
    }

    private fun setupDialog() {
        withdrawConfirmationDialogBinding = DialogWithdrawFundsConfirmationBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        withdrawFundsDialog = createFullScreenDialog(withdrawConfirmationDialogBinding, R.drawable.slider_bg, true)
        withdrawConfirmationDialogBinding.confirmBtn.setOnClickListener { withdrawFunds() }
    }

    private fun loadMoreTransactions() {
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()

        /**********************Retrofit to call get past withdraws funds Endpoint *********************/
        var call: Call<LoanRepaymentResponse>? = apiRequests?.getPastWithdraw(
            Prefs.getString("token"),
            generateRequestId(),
            "getWithdrawHistory"
        )
        call!!.enqueue(object : Callback<LoanRepaymentResponse> {
            override  fun onResponse(
                call: Call<LoanRepaymentResponse>,
                response: Response<LoanRepaymentResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        val newAddList = ArrayList<Transaction>()
                        adapter.addNewItems(newAddList)
                        for (i in response.body()!!.data!!){
                            newAddList.add(Transaction(txnId = i.txnId, txnAmt =i.txnAmt.toLong(), txnDate = i.txnDate))
                        }
                        adapter.addNewItems(newAddList)

                    }else{
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    binding.root.snackbar(getString(R.string.session_expired))
                    dialogLoader?.hideProgressDialog()
                    startAuth(navController)
                } else {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                }

            }

            override fun onFailure(call: Call<LoanRepaymentResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })

    }

    private fun checkInputs() {
        val amount = binding.etAmount.editText.editText?.text.toString()
        val recipient = binding.etRecipient.editText.editText?.text.toString()
        var error: String? = recipient.isPhoneNumberValid()

        if (amount.isEmpty()) error = "Amount cannot be empty!"
        if (amount.isNotEmpty() && amount.toLong() <= 0) error = "Amount cannot be negative or zero!"
        if (error != null) {
            binding.root.snackbar(error)
            return
        }
        mViewModel.setWithdraw(Withdraw(amount = amount.toLong(),phoneNumber = getString(R.string.phone_code)+recipient))


        withdrawConfirmationDialogBinding.valueText1.text = "John Doe"
        withdrawConfirmationDialogBinding.valueText2.text = recipient
        withdrawConfirmationDialogBinding.valueText3.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(amount.toLong()))
        withdrawConfirmationDialogBinding.valueText4.text = String.format(getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(950))
        withdrawFundsDialog.show()
    }

    private fun withdrawFunds() {
        withdrawFundsDialog.dismiss()
        navController.navigate(R.id.action_global_enterPinFragment, bundleOf(Config.LOGIN_TYPE to EnterPinType.WITHDRAW))
    }

}