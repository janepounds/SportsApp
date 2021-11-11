package com.example.flashsports.ui.fragments.businessInfo.businessProfile

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.BusinessDetailsResponse
import com.example.flashsports.databinding.FragmentEnterBusinessDetailsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
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
class EnterBusinessDetailsFragment : BaseFragment<FragmentEnterBusinessDetailsBinding>() {
    private  val TAG = "EnterBusinessDetailsFra"

    private val mViewModel: LoginViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentEnterBusinessDetailsBinding.inflate(inflater, container, false)

    override fun setupTheme() {

        loadBusinessDetails()
        binding.etDateRegistered?.editText?.let { addDatePicker(it,context) }
        binding.spinnerBusinessType.initSpinner(this)
        binding.spinnerIndustry.initSpinner(this)

    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutBusinessInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs(false) }
        binding.saveAndNextBtn.setOnClickListener { checkInputs(true) }
    }

    private fun loadBusinessDetails(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<BusinessDetailsResponse>? = apiRequests?.getBusinessDetails(
            Prefs.getString("token"),
            generateRequestId(),
            "getBusinessDetails"
        )
        call!!.enqueue(object : Callback<BusinessDetailsResponse> {
            override fun onResponse(
                call: Call<BusinessDetailsResponse>,
                response: Response<BusinessDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            binding.etBusinessName.editText?.setText(response.body()!!.data!!.business_name)
                            binding.etDateRegistered.editText?.setText(response.body()!!.data!!.reg_date)
                            binding.etRegistrationNo.editText?.setText(response.body()!!.data!!.reg_no)
                            binding.etLocation.editText?.setText(response.body()!!.data!!.location)
                            binding.etContactPerson.editText?.setText(response.body()!!.data!!.contact_person)
                            binding.etPhoneNumber.editText?.setText(
                                response.body()!!.data!!.phone_number.substring(
                                    3
                                )
                            )
                            binding.etNumberOfEmployees.editText?.setText(response.body()!!.data!!.no_employees)
                            binding.etAvgMonthlyRevenue.editText?.setText(response.body()!!.data!!.avg_monthly_revenue)
                            binding.spinnerBusinessType.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.business_type
                            binding.spinnerIndustry.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.industry
                        }else{
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }

                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }

                } else if (response.code() == 401) {
                    /***************redirect to auth*********************/
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                } else {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }

            }

            override fun onFailure(call: Call<BusinessDetailsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })


    }

    private fun checkInputs(proceedNext: Boolean) {
        val businessName = binding.etBusinessName.editText?.text.toString().trim()
        val dateRegistered = binding.etDateRegistered.editText?.text.toString().trim()
        val registrationNo = binding.etRegistrationNo.editText?.text.toString().trim()
        val location = binding.etLocation.editText?.text.toString().trim()
        val contactPerson = binding.etContactPerson.editText?.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.editText?.text.toString().trim()
        val numberOfEmployees = binding.etNumberOfEmployees.editText?.text.toString().trim()
        val avgMonthlyRevenue = binding.etAvgMonthlyRevenue.editText?.text.toString().trim()

        var industry: String? = null
        var businessType: String? = null
        var error: String? = phoneNumber.isPhoneNumberValid()

        if (!binding.spinnerIndustry.text.equals(getString(R.string.select))) {
            industry = binding.spinnerIndustry.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.industry))
        }

        if (!binding.spinnerBusinessType.text.equals(getString(R.string.select))) {
            businessType = binding.spinnerBusinessType.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.business_type))
        }

        if (avgMonthlyRevenue.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.avg_monthly_revenue))
        if (numberOfEmployees.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.number_of_employees))
        if (contactPerson.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.contact_person))
        if (location.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.location))
        if (registrationNo.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.registration_no))
        if (dateRegistered.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.date_of_birth))
        if (businessName.isEmpty()) error = String.format(getString(R.string.cannot_be_empty_error), getString(R.string.business_name))

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        if (proceedNext) {
            dialogLoader?.showProgressDialog()
            var call: Call<BusinessDetailsResponse>? = apiRequests?.postBusinessDetails(
                Prefs.getString("token"),
                businessName,businessType,registrationNo,dateRegistered,industry,location,contactPerson,getString(R.string.phone_code)+phoneNumber,
                numberOfEmployees,avgMonthlyRevenue.toDouble(),
                generateRequestId(),
                "saveBusinessDetails"
            )
            call!!.enqueue(object : Callback<BusinessDetailsResponse> {
                override fun onResponse(
                    call: Call<BusinessDetailsResponse>,
                    response: Response<BusinessDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        dialogLoader?.hideProgressDialog()

                        if (response.body()!!.status == 1) {
                            /************save values reg and location in the shared preferences*****************/
                            Prefs.putString("date_registered",dateRegistered)
                            Prefs.putString("location",location)
                            Prefs.putString("biz_name",businessName)
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            navController.navigate(R.id.action_enterBusinessDetailsFragment_to_verificationDocumentsFragment)

                        } else {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()

                        }

                    } else if (response.code() == 401) {
                        /***************redirect to auth*********************/
                        dialogLoader?.hideProgressDialog()
                        binding.root.snackbar(getString(R.string.session_expired))
                        startAuth(navController)


                    } else {  if(response.body()!!.message?.isNotEmpty()) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()
                    }
                    }

                }

                override fun onFailure(call: Call<BusinessDetailsResponse>, t: Throwable) {
                    t.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()


                }
            })

        }
    }


}