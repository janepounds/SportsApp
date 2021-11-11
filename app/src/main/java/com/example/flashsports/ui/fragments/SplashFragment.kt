package com.example.flashsports.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.models.responses.AuthenticationResponse
import com.example.flashsports.data.models.responses.PagesDetails
import com.example.flashsports.data.models.responses.StaticPagesResponse
import com.example.flashsports.databinding.FragmentSplashBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.utils.generateRequestId
import com.example.flashsports.utils.navigateUsingPopUp
import com.example.flashsports.utils.snackbar
import com.example.flashsports.utils.startAuth
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_SMS,
        Manifest.permission.CAMERA
    )

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSplashBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        requestStaticPagesData()
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL)
        }
    }

    override fun setupClickListeners() {}

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            val isLoggedIn = userPreferences.isLoggedIn.first()
            delay(1500)

            withContext(Dispatchers.Main) {
                if (isLoggedIn == true) {
                    /************check if token expired*******************/
                    if(Prefs.getString("token")==null){
                        navController.popBackStack(R.id.homeFragment, false)
                        navController.navigateUsingPopUp(R.id.splashFragment, R.id.action_splashFragment_to_enterPinFragment,
                            bundleOf(Config.LOGIN_TYPE to EnterPinType.LOGIN))
                    }
                    else
                        navController.navigateUsingPopUp(R.id.splashFragment, R.id.action_global_homeFragment)

                } else {
                    val showIntro = userPreferences.showIntro.first()
                    if (showIntro == false) {
                        navController.navigateUsingPopUp(R.id.splashFragment, R.id.action_global_welcomeFragment)
                    } else {
                        navController.navigate(R.id.action_splashFragment_to_introFragment)
                    }
                }
            }
        }
    }

    private fun requestStaticPagesData(){

        Constants.LOAN_DISCLOSURE = getString(R.string.loren_ipsum)
        Constants.TERMS_SERVICES = getString(R.string.loren_ipsum)
        Constants.LOAN_AGREEMENT = getString(R.string.loren_ipsum)
        Constants.RECOVERY_POLICY = getString(R.string.loren_ipsum)
        Constants.TERMS_AND_CONDITIONS = getString(R.string.loren_ipsum)

        var call: Call<StaticPagesResponse>? = apiRequests?.getStaticPages(generateRequestId(),"getStaticPages")

        try {
            call!!.enqueue(object : Callback<StaticPagesResponse> {
                override fun onResponse(
                    call: Call<StaticPagesResponse>,
                    response: Response<StaticPagesResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            response.body()!!.pagesData.let {
                                Constants.TERMS_AND_CONDITIONS = it!!.terms_and_conditions
                                Constants.LOAN_AGREEMENT = it!!.loan_agreement
                                Constants.RECOVERY_POLICY = it!!.recovery_policy
                            }
                        }

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

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}