package com.example.flashsports.ui.fragments.navigation

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.BalanceAndPaymentResponse
import com.example.flashsports.data.models.responses.CreditScoreResponse
import com.example.flashsports.data.models.responses.StaticPagesResponse
import com.example.flashsports.data.models.screen.ScreenItem
import com.example.flashsports.databinding.FragmentHomeBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.adapters.screen.HomeViewPagerAdapter
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.*
import com.example.flashsports.utils.calculation.CalculationUtils
import com.freshchat.consumer.sdk.Freshchat
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private  val TAG = "HomeFragment"
    private val mViewModel: LoanViewModel by activityViewModels()
    private val viewPagerList: ArrayList<ScreenItem> = ArrayList()
    private var currentPos = 0
    private var timer: Timer? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        runnable?.let { startSliderShow() }
    }

    override fun onPause() {
        super.onPause()
        runnable?.let {
            stopSlideShow()
            handler?.removeCallbacks(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runnable?.let {
            stopSlideShow()
            handler?.removeCallbacks(it)
        }
    }

    override fun setupTheme() {
        setupViewPager()
        getCreditScore()
        refreshUserData()
        requestStaticPagesData()
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
        Glide.with(requireContext()).load(Prefs.getString("profile_pic"))
            .apply(options).into(binding.userImage)

        /************get user from shared preferences********************/
        lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main) {
                    context?.let {
                        mViewModel.getCurrentUser(false, it)
                            .observe(viewLifecycleOwner, { user ->
                                binding.user = user
                                binding.valueWalletBalance.text = String.format(
                                    getString(R.string.wallet_balance_value),
                                    user.walletBalance
                                )
                                binding.tvPaymentDueAmt.text = String.format(getString(R.string.wallet_balance_value),
                                    MyApplication.getNumberFormattedString(user.payment_due!!))
                                if (user.payment_due_date != null) {
                                    binding.tvPaymentDueDate.text =
                                        formatDate(user.payment_due_date!!)
                                    binding.tvNextPaymentDate.text = String.format(
                                        getString(R.string.next_payment_date),
                                        formatDate(user.payment_due_date!!)
                                    )
                                }
                            })
                    }
                }
        }

    }

    override fun setupClickListeners() {
        binding.withdrawFundsCardView.setOnClickListener { navController.navigate(R.id.action_homeFragment_to_withdrawFundsFragment) }
        binding.myLoansCardView.setOnClickListener { navController.navigate(R.id.action_homeFragment_to_myLoansFragment) }
        binding.livechatMessaging.setOnClickListener{ Freshchat.showConversations(requireContext())}
    }
    private fun refreshUserData(){
        var call: Call<BalanceAndPaymentResponse>? = apiRequests?.refreshUserData(
            Prefs.getString("token"), generateRequestId(),"refreshUserData"
        )
        call!!.enqueue(object : Callback<BalanceAndPaymentResponse> {
            override  fun onResponse(
                call: Call<BalanceAndPaymentResponse>,
                response: Response<BalanceAndPaymentResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        if(response.body()!!.data !=null)
                        response.body()!!.data?.let {
                            binding.valueWalletBalance.text = String.format(
                                getString(R.string.wallet_balance_value), it.balance
                            )
                            binding.tvPaymentDueAmt.text = String.format(
                                getString(R.string.wallet_balance_value),
                                MyApplication.getNumberFormattedString(it.paymentAmountDue!!)
                            )
                            if (it.nextPaymentDate != null) {
                                binding.tvPaymentDueDate.text = formatDate(it.nextPaymentDate!!)
                                binding.tvNextPaymentDate.text = String.format(
                                    getString(R.string.next_payment_date),
                                    formatDate(it.nextPaymentDate!!)
                                )
                            }
                            Prefs.putString("business_status", it.business_status.toString())

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

            override fun onFailure(call: Call<BalanceAndPaymentResponse>, t: Throwable) {
                dialogLoader?.hideProgressDialog()

            }
        })
    }

    private fun requestStaticPagesData(){

        var call: Call<StaticPagesResponse>? = apiRequests?.getUserStaticPages(Prefs.getString("token"),generateRequestId(),"getStaticPages")

        try {
            call!!.enqueue(object : Callback<StaticPagesResponse> {
                override fun onResponse(
                    call: Call<StaticPagesResponse>,
                    response: Response<StaticPagesResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            response.body()!!.pagesData.let {
                                Constants.USER_LOAN_AGREEMENT = it!!.loan_agreement
                                Constants.USER_RECOVERY_POLICY = it!!.recovery_policy
                            }
                        }

                    }else if(response.code()==401){
                        dialogLoader?.hideProgressDialog()
                        binding.root.snackbar(getString(R.string.session_expired))
                        startAuth(navController)
                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }
                }

                override fun onFailure(call: Call<StaticPagesResponse>, t: Throwable) {
//                        t.message?.let { binding.root.snackbar(it) }

                }
            })

        } catch (e: IOException) {
            e.printStackTrace()
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
                        if(response.body()!!.data!!.credit_score <= 0){
                            Prefs.putString(
                                "credit_score",
                                0.toString()
                            )
                            Prefs.putString(
                                "max_loan_amount",
                                0.toString()
                            )
                        }else {
                            Prefs.putString(
                                "credit_score",
                                response.body()!!.data!!.credit_score.toString()
                            )
                            Prefs.putString(
                                "max_loan_amount",
                                CalculationUtils.calculateLoanAmount().toString()
                            )
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
                dialogLoader?.hideProgressDialog()

            }
        })
    }

    private fun setupViewPager() {
        viewPagerList.clear()
        viewPagerList.add(ScreenItem(drawableId = R.drawable.image_get_loan, title = getString(R.string.need_a_loan), subtitle = getHomeViewPagerHtmlText()))
        viewPagerList.add(ScreenItem(drawableId = R.drawable.image_get_loan, title = getString(R.string.need_a_loan), subtitle = getHomeViewPagerHtmlText()))
        viewPagerList.add(ScreenItem(drawableId = R.drawable.image_get_loan, title = getString(R.string.need_a_loan), subtitle = getHomeViewPagerHtmlText()))

        val adapter = HomeViewPagerAdapter(viewPagerList) {
            navController.navigate(R.id.action_homeFragment_to_myLoansFragment)
        }
        binding.viewPager.apply {
            this.adapter = adapter
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPos = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> startSliderShow()
                    ViewPager2.SCROLL_STATE_DRAGGING -> stopSlideShow()
                }
            }
        }
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _: TabLayout.Tab, _: Int -> }
        TabLayoutMediator(binding.viewpagerIndicator, binding.viewPager, true, tabConfigurationStrategy).attach()

        initializeHandler()
    }

    private fun initializeHandler() {
        handler = Handler(Looper.getMainLooper()!!)
        runnable = Runnable {
            if (currentPos == viewPagerList.size) currentPos = 0
            binding.viewPager.setCurrentItem(currentPos++, true)
        }
    }

    private fun startSliderShow() {
        stopSlideShow()
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler?.post(runnable!!)
            }
        }, 300, 5000)
    }

    private fun stopSlideShow() {
        timer?.cancel()
    }


}