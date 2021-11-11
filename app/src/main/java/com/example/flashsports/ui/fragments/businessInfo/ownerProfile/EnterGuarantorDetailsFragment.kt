package com.example.flashsports.ui.fragments.businessInfo.ownerProfile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.GuarantorResponse
import com.example.flashsports.databinding.FragmentEnterGuarantorDetailsBinding
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
class EnterGuarantorDetailsFragment : BaseFragment<FragmentEnterGuarantorDetailsBinding>() {

    private val mViewModel: LoginViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentEnterGuarantorDetailsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        loadGuarantorDetails()
        binding.firstKin.spinnerGender.initSpinner(this)
        binding.firstKin.spinnerRelationship.initSpinner(this)
        binding.secondKin.spinnerGender.initSpinner(this)
        binding.secondKin.spinnerRelationship.initSpinner(this)
    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutOwnerInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs(false) }
        binding.saveAndNextBtn.setOnClickListener { checkInputs(true) }
    }

    private fun loadGuarantorDetails(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<GuarantorResponse>? = apiRequests?.getGuarantorDetails(
            Prefs.getString("token"),
            generateRequestId(),
            "getGuarantors"
        )
        call!!.enqueue(object : Callback<GuarantorResponse> {
            override fun onResponse(
                call: Call<GuarantorResponse>,
                response: Response<GuarantorResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            binding.firstKin.etFullName.editText?.setText(response.body()!!.data!!.name)
                            binding.firstKin.spinnerGender.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.gender
                            binding.firstKin.spinnerRelationship.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.relationship
                            binding.firstKin.etMobileNumber.etPhoneNumber.editText?.setText(
                                response.body()!!.data!!.mobile?.substring(
                                    3
                                )
                            )
                            binding.firstKin.etResidentialAddress.editText?.setText(response.body()!!.data!!.residential_address)
                            binding.secondKin.etFullName.editText?.setText(response.body()!!.data!!.name2)
                            binding.secondKin.spinnerGender.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.gender2
                            binding.secondKin.spinnerRelationship.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.relationship2
                            binding.secondKin.etMobileNumber.etPhoneNumber.editText?.setText(
                                response.body()!!.data!!.mobile2?.substring(3)
                            )
                            binding.secondKin.etResidentialAddress.editText?.setText(response.body()!!.data!!.residential_address2)
                        }else{

                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }

                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }

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

            override fun onFailure(call: Call<GuarantorResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })

    }

    private fun checkInputs(proceedNext: Boolean) {
        val fullName1 = binding.firstKin.etFullName.editText?.text.toString().trim()
        val fullName2 = binding.secondKin.etFullName.editText?.text.toString().trim()
        val phone1 = binding.firstKin.etMobileNumber.etPhoneNumber.editText?.text.toString().trim()
        val phone2 = binding.secondKin.etMobileNumber.etPhoneNumber.editText?.text.toString().trim()
        val residential1 = binding.firstKin.etResidentialAddress.editText?.text.toString().trim()
        val residential2 = binding.secondKin.etResidentialAddress.editText?.text.toString().trim()
        var gender1: String? = null
        var gender2: String? = null
        var relationship1: String? = null
        var relationship2: String? = null


        var error: String? = null
        if (!binding.firstKin.spinnerGender.text.equals(getString(R.string.select))) {
            gender1 = binding.firstKin.spinnerGender.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.gender))
        }
        if (!binding.secondKin.spinnerGender.text.equals(getString(R.string.select))) {
            gender2 = binding.secondKin.spinnerGender.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.gender))
        }

        if (!binding.firstKin.spinnerRelationship.text.equals(getString(R.string.select))) {
            relationship1 = binding.firstKin.spinnerRelationship.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.education_level))
        }
        if (!binding.secondKin.spinnerRelationship.text.equals(getString(R.string.select))) {
            relationship2 = binding.secondKin.spinnerRelationship.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.education_level))
        }


        if (fullName1.isEmpty()) error = getString(R.string.full_name_cannot_be_empty)
        if (fullName2.isEmpty()) error = getString(R.string.full_name_cannot_be_empty)
        if (phone1.isEmpty()) error =  getString(R.string.phone_cannot_be_empty)
        if (phone2.isEmpty()) error =  getString(R.string.phone_cannot_be_empty)
        if (residential1.isEmpty()) error =  String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.residential_address)
        )
        if (residential2.isEmpty()) error =  String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.residential_address)
        )

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        if (proceedNext){
            dialogLoader?.showProgressDialog()
            /***************endpoint for updating guarantor details*********************/
            var call: Call<GuarantorResponse>? = apiRequests?.postGuarantorDetails(
                Prefs.getString("token"),fullName1,gender1,relationship1,getString(R.string.phone_code)+phone1,residential1,fullName2,gender2,relationship2,getString(R.string.phone_code)+phone2,residential2,
                "saveGuarantors",
                generateRequestId(),

            )
            call!!.enqueue(object : Callback<GuarantorResponse> {
                override fun onResponse(
                    call: Call<GuarantorResponse>,
                    response: Response<GuarantorResponse>
                ) {
                    if (response.isSuccessful) {
                        dialogLoader?.hideProgressDialog()
                        if (response.body()!!.status == 1) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            navController.navigate(R.id.action_enterGuarantorDetailsFragment_to_uploadIdDocumentsFragment)
                        } else {  if(response.body()!!.message?.isNotEmpty()) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                        }

                    } else if(response.code()==401) {
                        dialogLoader?.hideProgressDialog()
                        binding.root.snackbar(getString(R.string.session_expired))
                        startAuth(navController)
                    }else{  if(response.body()!!.message?.isNotEmpty()) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()
                    }
                    }

                }

                override fun onFailure(call: Call<GuarantorResponse>, t: Throwable) {
                    t.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()

                }
            })

        }

    }

}