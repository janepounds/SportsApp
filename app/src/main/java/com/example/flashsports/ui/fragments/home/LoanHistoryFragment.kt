package com.example.flashsports.ui.fragments.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.enums.LoanStatus
import com.example.flashsports.data.models.Loan
import com.example.flashsports.data.models.responses.LoanResponse
import com.example.flashsports.databinding.FragmentLoanHistoryBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.adapters.LoanAdapter
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.DialogLoader
import com.example.flashsports.utils.generateRequestId
import com.example.flashsports.utils.snackbar
import com.example.flashsports.utils.startAuth
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class LoanHistoryFragment : BaseFragment<FragmentLoanHistoryBinding>() {

    private val mViewModel: LoanViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var adapter: LoanAdapter
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoanHistoryBinding.inflate(inflater, container, false)

    override fun setupTheme() {

        adapter = LoanAdapter {
            mViewModel.setLoan(it)
            if(mViewModel.getLoan()?.status ==  LoanStatus.APPROVED || mViewModel.getLoan()?.status ==  LoanStatus.PAID || mViewModel.getLoan()?.status ==  LoanStatus.PARTIALLY_PAID){
                navController.navigate(R.id.action_global_loanDetailsFragment)
            }

        }
        binding.adapter = adapter

        getLoanHistory()

    }

    private fun getLoanHistory(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
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
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        if(response.body()!!.data.isNullOrEmpty()) {
                            val isListEmpty = adapter.itemCount == 0
                            showEmptyList(isListEmpty)
                        }
                        val list = ArrayList<Loan>()
                        for (i in response.body()!!.data!!){
                            var loanStatus=""
                            loanStatus = if(i.status.equals("Partially Paid",ignoreCase = true)){
                                "Partially_Paid"

                            }else{
                                i.status
                            }


                            list.add(Loan(loanId = i.loan_id, status = LoanStatus.valueOf(loanStatus.uppercase()), amt = i.amount.toLong(),interestRate = i.interest_rate,duration = i.duration,i.duration_units!!,loanDueAmount = i.amount_due.toLong(),paidToDate = i.paid_to_date))
                        }
                        adapter.updateList(list)





                    }else{
                        dialogLoader?.hideProgressDialog()
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                } else{  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<LoanResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })



    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
           binding.noInternetLayout.visibility = View.VISIBLE
        } else {
            binding.noInternetLayout.visibility = View.GONE
        }
    }

    override fun setupClickListeners() {
    }

}