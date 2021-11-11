package com.example.flashsports.ui.fragments.businessInfo.ownerProfile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.models.responses.IdDocumentResponse
import com.example.flashsports.databinding.FragmentUploadIdDocumentsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoginViewModel
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
import android.provider.MediaStore.Images
import android.content.ContentValues

import android.os.Build
import android.os.Build.VERSION_CODES.Q


@AndroidEntryPoint
class UploadIdDocumentsFragment : BaseFragment<FragmentUploadIdDocumentsBinding>() {
    private  val TAG = "UploadIdDocumentsFragme"

    private val mViewModel: LoginViewModel by activityViewModels()
    private val  filesViewModel: FilesViewModel by activityViewModels()
    private var mode: Int = -1
    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    private var requestCode:Int = 0
    private var filePath:String = ""


    private val photosLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            filePath = if(requestCode ==1){
                //select from gallery
                val selectedImageUri = activityResult.data!!.data
                File(context?.let { getRealPathFromURI(it,selectedImageUri) }).toString()


            }else{
                //take a picture
                var imageBitmap = activityResult.data!!.extras!!.get("data") as Bitmap
                val tempUri: Uri? = getImageUri(requireContext(), imageBitmap)
                File(context?.let { getRealPathFromURI(it,tempUri) }).toString()

            }
            var documentType = ""
            when (mode) {
                1 -> {

                    binding.nationalIdFrontSide.showImage(filePath)
                    documentType = Config.NATIONAL_ID_FRONT

                }
                2 -> {
                    binding.nationalIdBackSide.showImage(filePath)
                    documentType = Config.NATIONAL_ID_BACK
                }
                3 -> {
                    binding.profilePhoto.showImage(filePath)
                    documentType = Config.PROFILE_PICTURE
                }
                4 -> {
                    binding.selfieInYourBusiness.showImage(filePath)
                    documentType = Config.SELFIE_IN_BUSINESS
                }
            }
                    dialogLoader = context?.let { DialogLoader(it) }
                    CoroutineScope(Dispatchers.Main).launch { // runs on UI thread
                        filesViewModel.saveSingleDocument(dialogLoader!!,filePath, documentType, {
                            binding.root.snackbar("success")
                        }, {
                            Timber.d("message: $it")

                            binding.root.snackbar("failed")
                        })

            }
        }
    }


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUploadIdDocumentsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        viewModel = ViewModelProvider(
            requireActivity(), ViewModelFactory.FileViewModelFactory(DataUtils.getUserRepo())
        ).get(FilesViewModel::class.java)
        loadIdDocuments()
    }

    private fun loadIdDocuments(){
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()
        var call: Call<IdDocumentResponse>? = apiRequests?.getIdDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "getIdDocuments"
        )
        call!!.enqueue(object : Callback<IdDocumentResponse> {
            override fun onResponse(
                call: Call<IdDocumentResponse>,
                response: Response<IdDocumentResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()

                    if (response.body()!!.status == 1) {
                        /************populate all fields in UI*****************/
                        if(response.body()!!.data !=null) {
                            binding.nationalIdFrontSide.updatePhotoLayout(response.body()!!.data?.national_id_back_thumb?.toUri())
                            binding.nationalIdBackSide.updatePhotoLayout(response.body()!!.data?.national_id_back?.toUri())
                            binding.profilePhoto.updatePhotoLayout(response.body()!!.data?.profile_picture?.toUri())
                            binding.selfieInYourBusiness.updatePhotoLayout(response.body()!!.data?.selfie_in_business?.toUri())
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

            override fun onFailure(call: Call<IdDocumentResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()


            }
        })


    }

    override fun setupClickListeners() {
        binding.progressLayout.layoutOwnerInfo.backBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.saveBtn.setOnClickListener { checkInputs() }
        binding.saveAndNextBtn.setOnClickListener { checkInputs() }
        binding.nationalIdFrontSide.cardView.setOnClickListener { openChooser(requireContext(),1) }
        binding.nationalIdBackSide.cardView.setOnClickListener { openChooser(requireContext(),2) }
        binding.profilePhoto.cardView.setOnClickListener { openChooser(requireContext(),3) }
        binding.selfieInYourBusiness.cardView.setOnClickListener { openChooser(requireContext(),4) }
    }

    private fun checkInputs() {
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()

        /*****************post documents and redirect to home*************************/

        dialogLoader?.showProgressDialog()
        var call: Call<IdDocumentResponse>? = apiRequests?.postIdDocuments(
            Prefs.getString("token"),
            generateRequestId(),
            "saveIdDocuments"
            )
        call!!.enqueue(object : Callback<IdDocumentResponse> {
            override fun onResponse(
                call: Call<IdDocumentResponse>,
                response: Response<IdDocumentResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        navController.navigateUsingPopUp(R.id.homeFragment, R.id.action_global_homeFragment)
                    } else {
                         if(response.body()!!.message?.isNotEmpty()) {
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

            override fun onFailure(call: Call<IdDocumentResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })


    }

    private fun openChooser(context: Context, photoMode: Int){
        mode = photoMode
        val options = arrayOf<CharSequence>("Take Photo","Choose from Gallery","Cancel")
        val builder:AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Picture")
        builder.setItems(options,DialogInterface.OnClickListener{dialog, item ->
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
        val path = Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    @SuppressWarnings("deprecation")
   private fun getRealPathFromURI(context: Context,uri: Uri?): String? {
        var path = ""
        if (context.contentResolver!= null) {
            val cursor: Cursor? = context.contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }


}