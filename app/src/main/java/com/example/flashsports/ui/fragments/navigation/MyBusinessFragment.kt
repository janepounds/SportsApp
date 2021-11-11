package com.example.flashsports.ui.fragments.navigation


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.flashsports.R
import com.example.flashsports.data.models.User
import com.example.flashsports.data.models.responses.MyBusinessResponse
import com.example.flashsports.data.models.screen.BusinessExpandableLayout
import com.example.flashsports.databinding.FragmentMyBusinessBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.utils.DialogLoader
import com.example.flashsports.utils.addToggleClickListeners
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.flashsports.utils.*

@AndroidEntryPoint
class MyBusinessFragment : BaseFragment<FragmentMyBusinessBinding>() {
    private  val TAG = "MyBusinessFragment"

    private val mViewModel: LoanViewModel by activityViewModels()
    private var dob:String =""
    private var nin:String =""
    private var regDate:String =""
    private var location:String =""
    private var tradeLincense:String =""
    private var regCert:String =""
    private var taxCert:String =""
    private var bizName:String =""
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentMyBusinessBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        updateBusinessLayoutValues(null)

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
        Glide.with(requireContext()).load(Prefs.getString("profile_pic"))
            .apply(options).into(binding.userImage)
        /***********get user from shared preferences********************/
                context?.let {
                    mViewModel.getCurrentUser(false, it).observe(viewLifecycleOwner, { user ->
                        binding.user = user
                        updateBusinessLayoutValues(user)
                    })
                }

        dob = if(Prefs.getString("dob").isNotEmpty()){
            Prefs.getString("dob")
        }else{
            "Not set"

        }
        nin = if(Prefs.getString("nin").isNotEmpty()){
           Prefs.getString("nin")
        }else{
            "Not set"

        }
        bizName =  if(Prefs.getString("biz_name").isNotEmpty()){
             Prefs.getString("biz_name")

        }else{
             "Not set"
        }
        location = if(Prefs.getString("location").isNotEmpty()){
            Prefs.getString("location")
        }else{
            "Not set"

        }
        tradeLincense = if(Prefs.getString("trade_license").isNotEmpty()){
           "Uploaded"
        }else{
             "Not uploaded"
        }
        regCert = if(Prefs.getString("reg_certificate").isNotEmpty()){
            "Uploaded"
        }else{
            "Not uploaded"

        }
        taxCert = if(Prefs.getString("tax_reg_certificate").isNotEmpty()){
            "Uploaded"
        }else{
           "Not Uploaded"

        }
        regDate = if(Prefs.getString("date_registered").isNotEmpty()){
             Prefs.getString("date_registered")
        }else{
             "Not set"
        }
        getMyBusinessDetails()
    }



    override fun setupClickListeners() {
        binding.layoutOwnerProfile.addToggleClickListeners {
            navController.navigate(R.id.action_myBusinessFragment_to_enterPersonalDetailsFragment)
        }
        binding.layoutBusinessProfile.addToggleClickListeners {
            navController.navigate(R.id.action_myBusinessFragment_to_enterBusinessDetailsFragment)
        }
        binding.layoutBusinessDocuments.addToggleClickListeners {
            navController.navigate(R.id.action_myBusinessFragment_to_uploadBusinessDocumentsFragment)
        }
    }

    private fun getMyBusinessDetails(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
                var call: Call<MyBusinessResponse>? = apiRequests?.getMyBusinessDetails(
            Prefs.getString("token"), generateRequestId(),"getBriefProfile"
        )
        call!!.enqueue(object : Callback<MyBusinessResponse> {
            override  fun onResponse(
                call: Call<MyBusinessResponse>,
                response: Response<MyBusinessResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        if(response.body()!!.data !=null) {
                            response.body()!!.data?.let {
                                binding.layoutOwnerProfile.valueText2.text = it.dob
                                binding.layoutOwnerProfile.valueText3.text = it.nin
                                binding.layoutBusinessProfile.valueText1.text = it.business_name
                                binding.layoutBusinessProfile.valueText2.text = it.regDate
                                binding.layoutBusinessProfile.valueText3.text = it.location
                                binding.layoutBusinessDocuments.valueText1.text = it.tradeLincense
                                binding.layoutBusinessDocuments.valueText2.text = it.regCert
                                binding.layoutBusinessDocuments.valueText3.text = it.taxCert
                            }
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

            override fun onFailure(call: Call<MyBusinessResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })

    }

    private  fun updateBusinessLayoutValues(user: User?) {
        binding.layoutOwnerProfile.businessExpandableItem = getOwnerProfileItem(
            valueOne = user?.fullName ?: "",
            valueTwo = dob,
            valueThree = nin
        )
        binding.layoutBusinessProfile.businessExpandableItem = getBusinessProfileItem(
            valueOne = bizName,
            valueTwo = regDate,
            valueThree = location
        )
        binding.layoutBusinessDocuments.businessExpandableItem = getBusinessDocumentsItem(
            valueOne = tradeLincense,
            valueTwo = regCert,
            valueThree = taxCert
        )
    }

    private fun getOwnerProfileItem(valueOne: String, valueTwo: String, valueThree: String): BusinessExpandableLayout = BusinessExpandableLayout(
        title = getString(R.string.owner_profile),
        tv_text_1 = getString(R.string.name),
        tv_text_2 = getString(R.string.dob),
        tv_text_3 = getString(R.string.nin),
        value_text_1 = valueOne,
        value_text_2 = valueTwo,
        value_text_3 = valueThree
    )

    private fun getBusinessProfileItem(valueOne: String, valueTwo: String, valueThree: String): BusinessExpandableLayout = BusinessExpandableLayout(
        title = getString(R.string.business_profile),
        tv_text_1 = getString(R.string.name),
        tv_text_2 = getString(R.string.reg_date),
        tv_text_3 = getString(R.string.location),
        value_text_1 = valueOne,
        value_text_2 = valueTwo,
        value_text_3 = valueThree
    )

    private fun getBusinessDocumentsItem(valueOne: String, valueTwo: String, valueThree: String): BusinessExpandableLayout = BusinessExpandableLayout(
        title = getString(R.string.business_documents),
        tv_text_1 = getString(R.string.trade_license),
        tv_text_2 = getString(R.string.reg_certificate),
        tv_text_3 = getString(R.string.tax_certificate),
        value_text_1 = valueOne,
        value_text_2 = valueTwo,
        value_text_3 = valueThree
    )


}