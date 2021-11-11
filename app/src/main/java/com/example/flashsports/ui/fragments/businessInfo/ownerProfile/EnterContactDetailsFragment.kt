package com.example.flashsports.ui.fragments.businessInfo.ownerProfile

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.models.responses.ContactResponse
import com.example.flashsports.databinding.FragmentEnterContactDetailsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.LoginViewModel
import com.example.flashsports.utils.*
import com.pixplicity.easyprefs.library.Prefs
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@AndroidEntryPoint
class EnterContactDetailsFragment : BaseFragment<FragmentEnterContactDetailsBinding>() {

    private val mViewModel: LoginViewModel by activityViewModels()
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentEnterContactDetailsBinding.inflate(
        inflater,
        container,
        false
    )

    override fun setupTheme() {
        /*********load personal details from the server*************/
        loadContactDetails()
        binding.spinnerResidentialType.initSpinner(this)

        binding.spinnerResidentialType.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String?> { oldIndex, oldItem, newIndex, newItem ->
            if(binding.spinnerResidentialType.text.toString().trim().equals("Owner",ignoreCase = true)){
                binding.tvLandlordName.visibility = GONE
                binding.etLandlordName.visibility = GONE
                binding.tvLandlordPhoneNumber.visibility = GONE
                binding.etLandlordPhoneNumber.phone.visibility = GONE


            }else{
                binding.tvLandlordName.visibility = VISIBLE
                binding.etLandlordName.visibility = VISIBLE
                binding.tvLandlordPhoneNumber.visibility = VISIBLE
                binding.etLandlordPhoneNumber.phone.visibility = VISIBLE
            }
        })




        val districtListAdapter: ArrayAdapter<String>? =
            context?.let { ArrayAdapter<String>(it, android.R.layout.simple_dropdown_item_1line, listOf(*resources.getStringArray(R.array.residential_types))) }
        binding.autoCompleteDistrict.threshold = 1
        binding.autoCompleteDistrict.setAdapter(districtListAdapter)
        binding.autoCompleteDistrict.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.autoCompleteDistrict.showDropDown()
            }

        })
    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutOwnerInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs(false) }
        binding.saveAndNextBtn.setOnClickListener { checkInputs(true) }
    }

    private fun loadContactDetails(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<ContactResponse>? = apiRequests?.getContactDetails(
            Prefs.getString("token"),
            generateRequestId(),
            "getContactDetails"
        )
        call!!.enqueue(object : Callback<ContactResponse> {
            override fun onResponse(
                call: Call<ContactResponse>,
                response: Response<ContactResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            binding.etDistrict.editText?.setText(response.body()!!.data!!.district)
                            binding.etVillage.editText?.setText(response.body()!!.data!!.village)
                            binding.etMobileNumber.etPhoneNumber?.editText?.setText(
                                response.body()!!.data!!.mobile_phone.substring(
                                    3
                                )
                            )
                            binding.spinnerResidentialType.getSpinnerAdapter<String>().spinnerView.text =
                                response.body()!!.data!!.residential_type
                            binding.etLandlordPhoneNumber.etPhoneNumber.editText?.setText(
                                response.body()!!.data!!.landlord_contact?.substring(
                                    3
                                )
                            )
                            binding.etLandlordName.editText?.setText(response.body()!!.data!!.landlord)

                        }else{

                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }

                    }else{
                        if(response.body()!!.message?.isNotEmpty()) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()
                    }

                    }

                } else if(response.code()==401) {
                    /***************redirect to auth*********************/
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)


                }else{  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })


    }

    private fun checkInputs(proceedNext: Boolean) {
        val district = binding.autoCompleteDistrict?.text.toString().trim()
        val village = binding.etVillage.editText?.text.toString().trim()
        val mobileNumber = binding.etMobileNumber.etPhoneNumber.editText?.text.toString().trim()
        val landLordName = binding.etLandlordName.editText?.text.toString().trim()
        val landlordPhoneNumber = binding.etLandlordPhoneNumber.etPhoneNumber.editText?.text.toString().trim()
        var residentialType: String? = null
        var error: String? =null;

        val mobileNumberError = mobileNumber.isPhoneNumberValid()

        if (!binding.spinnerResidentialType.text.equals(getString(R.string.select))) {
            residentialType = binding.spinnerResidentialType.text.toString().trim()
        } else {
            error = String.format(
                getString(R.string.select_error),
                getString(R.string.residential_type)
            )
        }

        if (mobileNumberError != null) error = mobileNumberError
        if(residentialType.equals("Tenant",ignoreCase = true)) {
            var landlordNumber= landlordPhoneNumber.isPhoneNumberValid()
            if (landlordPhoneNumber.isEmpty()) error = String.format(
                getString(R.string.cannot_be_empty_error), getString(
                    R.string.land_lord_phone_number
                )
            )
            if (landLordName.isEmpty()) error = String.format(
                getString(R.string.cannot_be_empty_error), getString(
                    R.string.land_lord_name
                )
            )
        }
        if (mobileNumber.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error), getString(
                R.string.mobile_number
            )
        )
        if (village.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error), getString(
                R.string.village
            )
        )
        if (district.isEmpty()) error = String.format(
            getString(R.string.cannot_be_empty_error), getString(
                R.string.district
            )
        )

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }
        if (proceedNext){
            dialogLoader?.showProgressDialog()
            /***************endpoint for updating contact details*********************/
            var call: Call<ContactResponse>? = apiRequests?.postContactDetails(
                Prefs.getString("token"),
                district,village,residentialType,getString(R.string.phone_code)+mobileNumber,landLordName,getString(R.string.phone_code)+landlordPhoneNumber,
                generateRequestId(),
                "saveContactDetails"
            )
            call!!.enqueue(object : Callback<ContactResponse> {
                override fun onResponse(
                    call: Call<ContactResponse>,
                    response: Response<ContactResponse>
                ) {
                    if (response.isSuccessful) {
                        dialogLoader?.hideProgressDialog()
                        if (response.body()!!.status == 1) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            navController.navigate(R.id.action_enterContactDetailsFragment_to_enterGuarantorDetailsFragment)
                        } else {  if(response.body()!!.message?.isNotEmpty()) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }
                        }

                    } else if(response.code()==401){
                        dialogLoader?.hideProgressDialog()
                        binding.root.snackbar(getString(R.string.session_expired))
                        startAuth(navController)
                    }else{  if(response.body()!!.message?.isNotEmpty()) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()
                    }
                    }

                }

                override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                    t.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()

                }
            })


        }
    }

}