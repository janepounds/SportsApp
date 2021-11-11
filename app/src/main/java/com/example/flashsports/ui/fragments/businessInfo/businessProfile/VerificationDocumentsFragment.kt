package com.example.flashsports.ui.fragments.businessInfo.businessProfile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.models.responses.VerificationDocumentsResponse
import com.example.flashsports.databinding.FragmentVerificationDocumentsBinding
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
import java.io.ByteArrayOutputStream
import java.io.File


@AndroidEntryPoint
class VerificationDocumentsFragment : BaseFragment<FragmentVerificationDocumentsBinding>() {

    private val  filesViewModel: FilesViewModel by activityViewModels()
    private var mode: Int = -1
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var officeShopPhotoUri: Uri? = null
    private var officeShopVideoUri: Uri? = null
    private var selfieShopOfficePhotoUri: Uri? = null
    private var neighbourhoodPhotoUri: Uri? = null
    private var utilityBillPhotoUri: Uri? = null
    private var dialogLoader: DialogLoader? = null
    private var requestCode:Int = 0
    private var filePath:String = ""
    private var selectedImageUri:Uri? = null




    private val photosLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            filePath = if(requestCode ==1){
                //select from gallery
                selectedImageUri = activityResult.data!!.data
                File(context?.let { getRealPathFromURI(it,selectedImageUri) }).toString()


            }else{
                //take a picture
                var imageBitmap = activityResult.data!!.extras!!.get("data") as Bitmap
                selectedImageUri = getImageUri(requireContext(), imageBitmap)
                File(context?.let { getRealPathFromURI(it,selectedImageUri) }).toString()

            }
            var documentType = ""
                when (mode) {
                    1 -> {
                        officeShopPhotoUri =selectedImageUri
                        binding.officeShopPhoto.showImage(filePath)
                        documentType= Config.BUSINESS_PHOTO
                    }
                    3 -> {
                        selfieShopOfficePhotoUri =selectedImageUri
                        binding.selfieShopOffice.showImage(filePath)
                        documentType= Config.SELFIE_IN_BUSINESS
                    }
                    4 -> {
                        neighbourhoodPhotoUri =selectedImageUri
                        binding.neighbourhoodPhoto.showImage(filePath)
                        documentType= Config.BUSINESS_NEIGHBOURHOOD_PHOTO
                    }
                    5 -> {
                        utilityBillPhotoUri =selectedImageUri
                        binding.utilityBill.showImage(filePath)
                        documentType= Config.UTILITY_BILL
                    }
                }
                dialogLoader = context?.let { DialogLoader(it) }
                CoroutineScope(Dispatchers.Main).launch{ // runs on UI thread
                    filesViewModel.saveSingleDocument(dialogLoader!!,filePath,documentType,{
                        binding.root.snackbar("success")
                    },{
                        Timber.d("message: $it")


                        binding.root.snackbar("failed")
                    })
                }

        }
    }



    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentVerificationDocumentsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        viewModel = ViewModelProvider(
            requireActivity(), ViewModelFactory.FileViewModelFactory(DataUtils.getUserRepo())
        ).get(FilesViewModel::class.java)
        loadVerificationDocuments()
    }

    private fun loadVerificationDocuments(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<VerificationDocumentsResponse>? = apiRequests?.getVerificationDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "getVerificationDocs"
        )
        call!!.enqueue(object : Callback<VerificationDocumentsResponse> {
            override fun onResponse(
                call: Call<VerificationDocumentsResponse>,
                response: Response<VerificationDocumentsResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            binding.officeShopPhoto.updatePhotoLayout(response.body()!!.data?.business_photo?.toUri())
                            binding.selfieShopOffice.updatePhotoLayout(response.body()!!.data?.selfie_in_business?.toUri())
                            binding.neighbourhoodPhoto.updatePhotoLayout(response.body()!!.data?.neighbourhood_photo?.toUri())
                            binding.utilityBill.updatePhotoLayout(response.body()!!.data?.utility_bill?.toUri())
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

            override fun onFailure(call: Call<VerificationDocumentsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })
    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutBusinessInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs() }
        binding.saveAndNextBtn.setOnClickListener { checkInputs() }
        binding.officeShopPhoto.cardView.setOnClickListener { openChooser(requireContext(),1) }
        binding.selfieShopOffice.cardView.setOnClickListener { openChooser(requireContext(),3)}
        binding.neighbourhoodPhoto.cardView.setOnClickListener { openChooser(requireContext(),4) }
        binding.utilityBill.cardView.setOnClickListener { openChooser(requireContext(),5) }
    }


    private fun checkInputs() {
        var error: String? = null
        if (officeShopPhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.office_shop_photo))
        if (officeShopVideoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.office_shop_video))
        if (selfieShopOfficePhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.selfie_in_shop_office))
        if (neighbourhoodPhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.neighbourhood_photo))
        if (utilityBillPhotoUri == null) error = String().format(getString(R.string.photos_error), getString(R.string.utility_bill))

        if (!error.isNullOrEmpty()) {
            binding.root.snackbar(error)
            return
        }

        dialogLoader?.showProgressDialog()
        var call: Call<VerificationDocumentsResponse>? = apiRequests?.postVerificationDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "saveVerificationDocs"
        )
        call!!.enqueue(object : Callback<VerificationDocumentsResponse> {
            override fun onResponse(
                call: Call<VerificationDocumentsResponse>,
                response: Response<VerificationDocumentsResponse>
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

            override fun onFailure(call: Call<VerificationDocumentsResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })

    }

    private fun openChooser(context: Context, photoMode: Int){
        mode = photoMode
        val options = arrayOf<CharSequence>("Take Photo","Choose from Gallery","Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Picture")
        builder.setItems(options, DialogInterface.OnClickListener{ dialog, item ->
            when{
                options[item] == "Take Photo" -> {
                    requestCode = 0
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    photosLauncher.launch(takePicture)
                }
                options[item] == "Choose from Gallery" -> {
                    requestCode = 1
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    photosLauncher.launch(pickPhoto)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        })
        builder.show()

    }

    @SuppressWarnings("deprecation")
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }



    private fun getRealPathFromURI(context: Context,uri: Uri?): String? {
        var path = ""
        if (context.contentResolver!= null) {
            val cursor: Cursor? = context.contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }




}