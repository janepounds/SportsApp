package com.example.flashsports.ui.fragments.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.CreditScoreResponse
import com.example.flashsports.data.models.screen.CreditFactorItem
import com.example.flashsports.databinding.FragmentCreditScoreBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.adapters.screen.CreditFactorAdapter
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.calculation.CalculationUtils
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


import android.content.ContentResolver
import com.example.flashsports.data.models.Sms
import com.example.flashsports.utils.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class CreditScoreFragment : BaseFragment<FragmentCreditScoreBinding>() {

    private  val TAG = "CreditScoreFragment"
    private val mViewModel: LoginViewModel by activityViewModels()
    private lateinit var adapter: CreditFactorAdapter
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentCreditScoreBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.layoutCreditScore.creditScoreValue.text = 0f.toString()
        binding.layoutCreditScore.tvGoodScore.text = String.format("Loan Limit "+
            MyApplication.getAppContext().getString(R.string.wallet_balance_value), MyApplication.getNumberFormattedString(
            CalculationUtils.calculateLoanAmount()))

        getCreditScore()

        val list = ArrayList<CreditFactorItem>()
        list.add(CreditFactorItem(color = R.color.green, title = getString(R.string.revenue), percentage = 10.0))
        list.add(CreditFactorItem(color = R.color.violet, title = getString(R.string.business_documents), percentage = 15.0))
        list.add(CreditFactorItem(color = R.color.orange, title = getString(R.string.business_assets), percentage = 15.0))
        list.add(CreditFactorItem(color = R.color.darkBlue_1, title = getString(R.string.credit_history), percentage = 20.0))
        list.add(CreditFactorItem(color = R.color.primaryColor, title = getString(R.string.payment_history), percentage = 20.0))
        list.add(CreditFactorItem(color = R.color.primaryColor, title = getString(R.string.financial_history), percentage = 20.0))
        adapter = CreditFactorAdapter(list)

        binding.recyclerView.adapter = adapter
    }

    override fun setupClickListeners() {
        binding.layoutCreditScore.creditScoreGauge.setOnEventListener { gauge, lowValue, highValue, isRunning ->
            Timber.e("Low: $lowValue  High: $highValue")
            binding.layoutCreditScore.creditScoreValue.text = (highValue * 10).toString()
        }
    }



    private fun getCreditScore(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        /**********************Retrofit to call new loan Endpoint *********************/
        var call: Call<CreditScoreResponse>? = apiRequests?.getCreditScore(
            Prefs.getString("token"), generateRequestId(),"getUserCreditScore"
        )
        call!!.enqueue(object : Callback<CreditScoreResponse> {
            override  fun onResponse(
                call: Call<CreditScoreResponse>,
                response: Response<CreditScoreResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        binding.layoutCreditScore.creditScoreValue.text  =
                            response.body()!!.data!!.credit_score.roundToInt().toString()
                        if(response.body()!!.data!!.credit_score <= 0){
                            binding.layoutCreditScore.tvGoodScore.text = String.format("Loan Limit "+
                                    MyApplication.getAppContext().getString(R.string.wallet_balance_value), 0)
                        }

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

            override fun onFailure(call: Call<CreditScoreResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })
    }


}