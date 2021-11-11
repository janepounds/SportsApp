package com.example.flashsports.ui.fragments.businessInfo.businessDocuments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.flashsports.R
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config.AUDITED_FINANCIALS
import com.example.flashsports.data.config.Config.BANK_STATEMENT
import com.example.flashsports.data.config.Config.BUSINESS_PLAN
import com.example.flashsports.data.config.Config.RECEIPT_BOOK
import com.example.flashsports.data.config.Config.REGISTRATION_CERTIFICATE
import com.example.flashsports.data.config.Config.TAX_CLEARANCE_CERTIFICATE
import com.example.flashsports.data.config.Config.TAX_REGISTRATION_CERTIFICATE
import com.example.flashsports.data.config.Config.TRADE_LICENSE
import com.example.flashsports.data.models.responses.BusinessDocumentsResponse
import com.example.flashsports.databinding.FragmentUploadBusinessDocumentsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.ViewModelFactory
import com.example.flashsports.utils.*
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

import android.view.LayoutInflater
import androidx.core.net.toUri
import com.example.flashsports.data.config.Config.CREDIT_REFERENCE_REPORT
import com.example.flashsports.data.config.Config.PROOF_OF_ASSETS
import java.lang.Exception
import android.widget.Toast
import com.example.flashsports.data.config.Config.EXPENDITURE_REPORT
import com.example.flashsports.data.config.Config.TAX_REPORT


@AndroidEntryPoint
class UploadBusinessDocumentsFragment : BaseFragment<FragmentUploadBusinessDocumentsBinding>() {


    private val TAG = "UploadBusinessDocuments"
    private val  mViewModel: FilesViewModel by activityViewModels()
    private var mode: Int = -1
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    private var tradeLicensePhotoUri: Uri? = null
    private var registrationCertificatePhotoUri: Uri? = null
    private var taxRegCertificatePhotoUri: Uri? = null
    private var taxClearanceCertificatePhotoUri: Uri? = null
    private var bankStatementPhotoUri: Uri? = null
    private var auditedFinancialsPhotoUri: Uri? = null
    private var businessPlanPhotoUri: Uri? = null
    private var receiptBookPhotoUri: Uri? = null
    private var proofOfAssetsPhotoUri: Uri? = null
    private var creditRefReportPhotoUri: Uri? = null
    private var taxReportPhotoUri: Uri? = null
    private var expenditureReportPhotoUri: Uri? = null






    private val fileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val data = activityResult.data
        if (activityResult.resultCode == Activity.RESULT_OK) {
            var documentType=""
            var filePath:String? = null
            var uri:Uri? = data?.data

            try {
                filePath = FileUtils.getPath(this.requireContext(), uri!!)
            } catch (e: Exception) {
                Log.e(TAG, "Error: $e")
                Toast.makeText(this.context, "Error: $e", Toast.LENGTH_SHORT).show()
            }
            var extension = filePath?.substring(filePath.lastIndexOf(".")+1)
            if(extension.equals("jpg",ignoreCase = true) || extension.equals("png", ignoreCase = true) ||extension.equals("webp", ignoreCase = true) ||extension.equals("pdf",ignoreCase = true) ) {
                when (mode) {
                    1 -> {
                        tradeLicensePhotoUri = data?.data
                        documentType = TRADE_LICENSE
                        binding.tradeLicense.showFile(filePath)
                    }

                    2 -> {
                        registrationCertificatePhotoUri = data?.data
                        binding.registrationCertificate.showFile(filePath)
                        documentType = REGISTRATION_CERTIFICATE
                    }
                    3 -> {
                        taxRegCertificatePhotoUri = data?.data
                        binding.taxRegCertificate.showFile(filePath)
                        documentType = TAX_REGISTRATION_CERTIFICATE

                    }
                    4 -> {
                        taxClearanceCertificatePhotoUri = data?.data
                        binding.taxClearanceCertificate.showFile(filePath)
                        documentType = TAX_CLEARANCE_CERTIFICATE
                    }
                    5 -> {
                        bankStatementPhotoUri = data?.data
                        binding.bankStatement.showFile(filePath)
                        documentType = BANK_STATEMENT
                    }
                    6 -> {
                        auditedFinancialsPhotoUri = data?.data
                        binding.auditedFinancials.showFile(filePath)
                        documentType = AUDITED_FINANCIALS
                    }
                    7 -> {
                        businessPlanPhotoUri = data?.data
                        binding.businessPlan.showFile(filePath)
                        documentType = BUSINESS_PLAN

                    }
                    8 -> {
                        receiptBookPhotoUri = data?.data
                        binding.receiptBook.showFile(filePath)
                        documentType = RECEIPT_BOOK
                    }

                    9 -> {
                        proofOfAssetsPhotoUri = data?.data
                        binding.proofOfAssets.showFile(filePath)
                        documentType = PROOF_OF_ASSETS
                    }
                    10 -> {
                        creditRefReportPhotoUri = data?.data
                        binding.creditReferenceReport.showFile(filePath)
                        documentType = CREDIT_REFERENCE_REPORT
                    }
                    11 -> {
                        taxReportPhotoUri = data?.data
                        binding.taxReport.showFile(filePath)
                        documentType = TAX_REPORT
                    }
                    12 -> {
                        expenditureReportPhotoUri = data?.data
                        binding.expenditureReport.showFile(filePath)
                        documentType = EXPENDITURE_REPORT
                    }
                }
                dialogLoader = context?.let { DialogLoader(it) }
                CoroutineScope(Dispatchers.Main).launch { // runs on UI thread
                    mViewModel.saveSingleDocument(dialogLoader!!, filePath!!, documentType, {
                        binding.root.snackbar("success")
                        when(documentType){
                            TRADE_LICENSE -> Prefs.putString(TRADE_LICENSE, data?.data.toString())
                            REGISTRATION_CERTIFICATE -> Prefs.putString(REGISTRATION_CERTIFICATE, data?.data.toString())
                            TAX_REGISTRATION_CERTIFICATE ->  Prefs.putString(TAX_REGISTRATION_CERTIFICATE, data?.data.toString())

                        }
                    }, {
                        Timber.d("message: $it")

                        binding.root.snackbar("failed")
                    })

                }
            }else{
                binding.root.snackbar("Incorrect File format")
            }
        }
    }


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUploadBusinessDocumentsBinding.inflate(inflater, container, false)



    override fun setupTheme() {
        viewModel = ViewModelProvider(
            requireActivity(), ViewModelFactory.FileViewModelFactory(DataUtils.getUserRepo())
        ).get(FilesViewModel::class.java)
        loadBusinessDocuments()
       
    }

    private fun loadBusinessDocuments(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<BusinessDocumentsResponse>? = apiRequests?.getBusinessDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "getBusinessDocs"
        )
        call!!.enqueue(object : Callback<BusinessDocumentsResponse> {
            override fun onResponse(
                call: Call<BusinessDocumentsResponse>,
                response: Response<BusinessDocumentsResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            response.body()!!.data?.let {
                                binding.tradeLicense.loadDocument(
                                    it.trade_license_thumb
                                )
                                binding.registrationCertificate.loadDocument(
                                    it.reg_certificate_thumb
                                )
                                binding.taxRegCertificate.loadDocument(
                                    it.tax_reg_certificate_thumb
                                )
                                binding.taxClearanceCertificate.loadDocument(
                                    it.tax_clearance_certificate_thumb
                                )
                                binding.bankStatement.loadDocument(
                                    it.bank_statement_thumb
                                )
                                binding.auditedFinancials.loadDocument(
                                    it.audited_financials_thumb
                                )
                                binding.receiptBook.loadDocument(
                                    it.receipt_book_thumb
                                )

                                binding.businessPlan.loadDocument(
                                    it.business_plan_thumb
                                )

                                binding.proofOfAssets.loadDocument(
                                    it.proof_of_assets_thumb
                                )
                                binding.creditReferenceReport.loadDocument(
                                    it.credit_reference_report_thumb
                                )
                                binding.taxReport.loadDocument(
                                    it.tax_report_thumb
                                )
                                binding.expenditureReport.loadDocument(
                                    it.expenditure_report_thumb
                                )
                            }
                        }else{
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()
                        }

                    } else {
                        if (response.body()!!.message.isNotEmpty()) {
                            response.body()!!.message?.let { binding.root.snackbar(it) }
                            dialogLoader?.hideProgressDialog()

                        }
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

            override fun onFailure(call: Call<BusinessDocumentsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })
    }

    override fun setupClickListeners() {
        binding.layoutOwnerInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs() }
        binding.saveAndNextBtn.setOnClickListener { checkInputs() }
        binding.tradeLicense.cardView.setOnClickListener {openFile(1) }
        binding.registrationCertificate.cardView.setOnClickListener { openFile(2) }
        binding.taxRegCertificate.cardView.setOnClickListener { openFile(3) }
        binding.taxClearanceCertificate.cardView.setOnClickListener { openFile(4)}
        binding.bankStatement.cardView.setOnClickListener { openFile(5)}
        binding.auditedFinancials.cardView.setOnClickListener { openFile(6) }
        binding.businessPlan.cardView.setOnClickListener {openFile(7) }
        binding.receiptBook.cardView.setOnClickListener { openFile(8) }
        binding.proofOfAssets.cardView.setOnClickListener { openFile(9) }
        binding.creditReferenceReport.cardView.setOnClickListener { openFile(10) }
    }



    private fun openFile(type:Int){
        mode = type
        var chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.type = "*/*"
        // Only return URIs that can be opened with ContentResolver
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file")
        fileLauncher.launch(chooseFileIntent)
    }



    private fun checkInputs() {
        var error: String? = null

        if (tradeLicensePhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.trade_license_text))
        if (registrationCertificatePhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.registration_certificate))


        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }

        dialogLoader?.showProgressDialog()
        var call: Call<BusinessDocumentsResponse>? = apiRequests?.postBusinessDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "saveBusinessDocs"
        )
        call!!.enqueue(object : Callback<BusinessDocumentsResponse> {
            override fun onResponse(
                call: Call<BusinessDocumentsResponse>,
                response: Response<BusinessDocumentsResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        navController.navigateUsingPopUp(R.id.homeFragment, R.id.action_global_homeFragment)

                    } else {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        dialogLoader?.hideProgressDialog()

                    }

                } else if (response.code() == 401) {
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)


                } else {  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<BusinessDocumentsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })

    }



}