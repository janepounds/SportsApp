package com.example.flashsports.ui.fragments.navigation

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebSettings
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import com.example.flashsports.databinding.FragmentAccountBinding
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.data.models.User
import com.example.flashsports.data.models.screen.AccountExpandableLayout
import com.example.flashsports.databinding.DialogLoanStatusBinding
import com.example.flashsports.databinding.DialogWebviewFullscreenBinding
import com.example.flashsports.ui.activities.MainActivity
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.*

import com.pixplicity.easyprefs.library.Prefs

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.CompoundButton
import android.widget.RadioGroup







@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>() {

    private val mViewModel: LoanViewModel by activityViewModels()
    private lateinit var statusDialogBinding:  DialogWebviewFullscreenBinding
    private lateinit var statusDialogBinding1:  DialogWebviewFullscreenBinding
    private lateinit var statusDialogBinding2:  DialogWebviewFullscreenBinding
    private lateinit var statusDialog: Dialog
    private lateinit var statusDialog1: Dialog
    private lateinit var statusDialog2: Dialog


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentAccountBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loadAppVersion()
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
        Glide.with(requireContext()).load(Prefs.getString("profile_pic"))
            .apply(options).into(binding.layoutAccountAbove.userImage)
        /************get user from shared preferences********************/

        context?.let {
            mViewModel.getCurrentUser( false, it).observe(viewLifecycleOwner, { user ->
                binding.layoutAccountAbove.user=user
            })
        }
        updateAccountDetails()
//        setupDialog()

    }


    override fun setupClickListeners() {
        binding.layoutAccountAbove.editBtn.setOnClickListener { updateAccountInfo()}
        binding.layoutCustomerSupport.addToggleClickListeners{
            if(binding.layoutCustomerSupport.tvText1.isChecked){
                navController.navigate(R.id.action_accountFragment_to_sendEmailFragment)

            }else if(binding.layoutCustomerSupport.tvText3.isChecked){
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:0123456789")
                startActivity(intent)
            }
        }
        binding.layoutAccountCategories.settingsCardView.setOnClickListener {navController.navigate(R.id.action_accountFragment_to_changePinFragment) }
        binding.layoutAccountCategories.faqCardView.setOnClickListener {navController.navigate(R.id.action_accountFragment_to_FAQsFragment) }
        binding.layoutLoanPolicy.addToggleClickListeners {
            binding.layoutLoanPolicy.tvText1.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    Constants.LOAN_DISCLOSURE?.let {
                        showDialog(requireContext(),
                            it,getString(R.string.lending_disclosure))
                    }
                }

            }
            binding.layoutLoanPolicy.tvText3.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    Constants.RECOVERY_POLICY?.let {
                        showDialog(requireContext(),
                            it,getString(R.string.recovery_policy))
                    }
                }

            }
            binding.layoutLoanPolicy.tvText2.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    Constants.LOAN_AGREEMENT?.let {
                        showDialog(requireContext(),
                            it,getString(R.string.loan_agreement))
                    }
                }

            }

        }
        binding.rateApp.setOnClickListener { rateAppFun() }
        binding.shareApp.setOnClickListener { shareAppFun() }
        binding.logoutBtn.setOnClickListener { logOut() }
    }

    private fun updateAccountInfo() {
        navController.navigate(R.id.action_accountFragment_to_updateAccountDetailsFragment)


    }
    private fun updateAccountDetails(){
        binding.layoutCustomerSupport.accountExpandableItem = getCustomerSupport()

        binding.layoutLoanPolicy.accountExpandableItem = getLoanPolicy()

    }


    private fun getCustomerSupport():AccountExpandableLayout = AccountExpandableLayout(
        title = getString(R.string.customer_support),
        tv_text_1 = getString(R.string.email),
        tv_text_2 = getString(R.string.live_chat),
        tv_text_3 = getString(R.string.toll_free),
        sub_title = getString(R.string.do_you_need_help),
        image = resources.getDrawable(R.drawable.ic_customer_support)

    )

    private fun getLoanPolicy():AccountExpandableLayout = AccountExpandableLayout(
        title = getString(R.string.loan_policy),
        tv_text_1 = getString(R.string.lending_disclosure),
        tv_text_2 = getString(R.string.loan_agreement),
        tv_text_3 = getString(R.string.recovery_policy),
        sub_title = getString(R.string.what_you_need_to_know),
        image = resources.getDrawable(R.drawable.ic_terms_and_conditions)

    )


    private fun loadAppVersion() {
        var version = "1.0"
        try {
            val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            version = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        binding.appVersion = version
    }


    private fun rateAppFun() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        }
    }

    private fun shareAppFun() {
        try {
            val shareAppIntent = Intent(Intent.ACTION_SEND)
            shareAppIntent.type = "text/plain"
            shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_intent_msg))
            val shareMessage = "http://play.google.com/store/apps/details?id=${requireContext().packageName}"
            shareAppIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareAppIntent, getString(R.string.app_name)))
        } catch (e: Exception) {
            binding.root.snackbar("Something went wrong! ${e.message}")
        }
    }

    private fun logOut() {
        Prefs.clear()
        lifecycleScope.launch {
            userPreferences.saveIsLoggedIn(false)
        }
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }



}