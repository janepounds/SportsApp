package com.example.flashsports.ui.fragments.businessInfo.ownerProfile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.RegistrationResponse
import com.example.flashsports.data.models.responses.UserResponse
import com.example.flashsports.databinding.FragmentEnterPersonalDetailsBinding
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
class EnterPersonalDetailsFragment : BaseFragment<FragmentEnterPersonalDetailsBinding>() {

    private  val TAG = "EnterPersonalDetailsFra"
    private val mViewModel: LoginViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentEnterPersonalDetailsBinding.inflate(inflater, container, false)


    override fun setupTheme() {
        /*********load personal details from the server*************/
        loadPersonalDetails()
        binding.etDateOfBirth?.editText?.let { addDatePicker(it,context) }
        binding.spinnerGender.initSpinner(this)
        binding.spinnerEducationLevel.initSpinner(this)
        binding.spinnerMaritalStatus.initSpinner(this)

    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutOwnerInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs(false) }
        binding.saveAndNextBtn.setOnClickListener { checkInputs(true) }
    }

    private fun loadPersonalDetails() {
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<UserResponse>? = apiRequests?.getPersonalDetails(
            Prefs.getString("token"),
            generateRequestId(),
            "getPersonalDetails"
        )
        call!!.enqueue(object : Callback<UserResponse> {
            override fun onResponse(
                call: Call<UserResponse>,
                response: Response<UserResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {

                            binding.etFullName.editText?.setText(response.body()!!.data.name)
                            binding.etDateOfBirth.editText?.setText(response.body()!!.data.dob)
                            binding.spinnerGender.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data.gender
                            binding.spinnerEducationLevel.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data.education_level
                            binding.spinnerMaritalStatus.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data.marital_status
                            binding.etNoOfDependants.editText?.setText(response.body()!!.data.no_of_dependents.toString())
                            binding.etYearInBusiness.editText?.setText(response.body()!!.data.years_in_business.toString())
                            binding.etNationalId.editText?.setText(response.body()!!.data.nin)
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


                } else {  if(response.body()!!.message?.isNotEmpty()==true) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })


    }

    private fun checkInputs(proceedNext: Boolean) {
        val fullName = binding.etFullName.editText?.text.toString().trim()
        val dateOfBirth = binding.etDateOfBirth.editText?.text.toString().trim()
        val yearsInBusiness = binding.etYearInBusiness.editText?.text.toString().trim()
        val nationalId = binding.etNationalId.editText?.text.toString().trim()
        val noOfDependants = binding.etNoOfDependants.editText?.text.toString().trim()
        var gender: String? = null
        var educationLevel: String? = null
        var maritalStatus: String? = null

        var error: String? = null
        if (!binding.spinnerGender.text.equals(getString(R.string.select))) {
            gender = binding.spinnerGender.text.toString().trim()
        } else {
            error = String.format(getString(R.string.select_error), getString(R.string.gender))
        }

        if (!binding.spinnerEducationLevel.text.equals(getString(R.string.select))) {
            educationLevel = binding.spinnerEducationLevel.text.toString().trim()
        } else {
            error =
                String.format(getString(R.string.select_error), getString(R.string.education_level))
        }

        if (!binding.spinnerMaritalStatus.text.equals(getString(R.string.select))) {
            maritalStatus = binding.spinnerMaritalStatus.text.toString().trim()
        } else {
            error =
                String.format(getString(R.string.select_error), getString(R.string.marital_status))
        }

        if (nationalId.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.national_id)
        )
        if (noOfDependants.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.no_of_dependants)
        )
        if (yearsInBusiness.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.years_in_business)
        )
        if (dateOfBirth.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error),
            getString(R.string.date_of_birth)
        )
        if (fullName.isEmpty()) error = getString(R.string.full_name_cannot_be_empty)

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        if (proceedNext) {
            dialogLoader?.showProgressDialog()
            /***************endpoint for updating personal details*********************/
            var call: Call<RegistrationResponse>? = apiRequests?.postPersonalDetails(
                Prefs.getString("token"),
                name = fullName,
                gender = gender,
                dateOfBirth,
                educationLevel,
                maritalStatus,
                years_in_business = yearsInBusiness.toInt(),
                nin = nationalId,
                noOfDependants.toInt(),
                generateRequestId(),
                "savePersonalDetails"

            )
            call!!.enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(
                    call: Call<RegistrationResponse>,
                    response: Response<RegistrationResponse>
                ) {
                    if (response.isSuccessful) {
                        dialogLoader?.hideProgressDialog()
                        if (response.body()!!.status == 1) {
                            /************save values dob and nin in the shared preferences*****************/

                            Prefs.putString("nin",nationalId)
                            Prefs.putString("dob",dateOfBirth)

                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            navController.navigate(R.id.action_enterPersonalDetailsFragment_to_enterContactDetailsFragment)
                        } else
                            {  if(response.body()!!.message?.isNotEmpty()==true) {
                                response.body()!!.message?.let { binding.root.snackbar(it) }
                                dialogLoader?.hideProgressDialog()
                            }
                            }



                    } else if(response.code()==401) {
                        dialogLoader?.hideProgressDialog()
                        binding.root.snackbar(getString(R.string.session_expired))
                        startAuth(navController)
                    }else{
                        if(response.body()!!.message?.isNotEmpty()==true) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                    }

                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    t.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()

                }
            })

        }

    }
}

