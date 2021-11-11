package com.example.flashsports.ui.fragments

import `in`.mayanknagwanshi.imagepicker.ImageSelectActivity
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.models.responses.UserAccountResponse
import com.example.flashsports.databinding.FragmentUpdateAcountDetailsBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.ui.viewModels.LoanViewModel
import com.example.flashsports.ui.viewModels.ViewModelFactory
import com.example.flashsports.utils.*
import com.google.android.material.textfield.TextInputEditText
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
class UpdateAccountDetailsFragment :  BaseFragment<FragmentUpdateAcountDetailsBinding>() {
    private val mViewModel: LoanViewModel by activityViewModels()
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
            var documentType=""
                    when (mode) {
                        1 -> {

                            documentType = Config.PROFILE_PICTURE

                            Glide.with(requireContext())
                                .load(filePath)
                                .into(binding.userImage)

                        }
                    }
                    dialogLoader = context?.let { DialogLoader(it) }
                    CoroutineScope(Dispatchers.Main).launch { // runs on UI thread
                        filesViewModel.saveSingleDocument(dialogLoader!!,filePath, documentType, {
                            binding.root.snackbar("success")
                            Prefs.putString("profile_pic", filePath)
                        }, {
                            Timber.d("file: $it")

                            binding.root.snackbar("failed")
                        })
            }
        }
    }
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUpdateAcountDetailsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        viewModel = ViewModelProvider(
            requireActivity(), ViewModelFactory.FileViewModelFactory(DataUtils.getUserRepo())
        ).get(FilesViewModel::class.java)
        /************get user from shared preferences********************/
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            Glide.with(requireContext()).load(Prefs.getString("profile_pic"))
            .apply(options).into(binding.userImage)
            context?.let {
            mViewModel.getCurrentUser( false, it).observe(viewLifecycleOwner, { user ->
                binding.user=user
            })
        }
    }

    override fun setupClickListeners() {
        binding.userImage.setOnClickListener { openChooser(requireContext(),1) }
        binding.tvPersonalDetailsEdit.setOnClickListener{activateTextViewEditable()}
        binding.confirmBtn.setOnClickListener{checkInputs()}
    }




    private fun activateTextViewEditable(){
        allowEditable(binding.tvMeailValue)
        allowEditable(binding.tvFullNameValue)

    }

    private fun checkInputs(){
        /**********endpoint to update account details**************/
        dialogLoader = context?.let { DialogLoader(it) }
        dialogLoader?.showProgressDialog()

        var call: Call<UserAccountResponse>? = apiRequests?.updateUserInfo(
            Prefs.getString("token"),
            binding.tvFullNameValue.text.toString(),
            binding.tvMeailValue.text.toString(),
            generateRequestId(),
            "updateUser"
        )
        call!!.enqueue(object : Callback<UserAccountResponse> {
            override  fun onResponse(
                call: Call<UserAccountResponse>,
                response: Response<UserAccountResponse>
            ) {
                if (response.isSuccessful) {
                    dialogLoader?.hideProgressDialog()
                    if (response.body()!!.status == 1) {
                        //update user preferences
                        Prefs.putString("email",binding.tvMeailValue.text.toString())
                        Prefs.putString("name",binding.tvFullNameValue.text.toString())
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                        navController.popBackStack()

                    }else{
                        dialogLoader?.hideProgressDialog()
                        response.body()!!.message?.let { binding.root.snackbar(it) }
                    }

                }else if(response.code()==401){
                    dialogLoader?.hideProgressDialog()
                    binding.root.snackbar(getString(R.string.session_expired))
                    startAuth(navController)
                } else {  if(response.body()!!.message?.isNotEmpty()) {
                    response.body()!!.message?.let { binding.root.snackbar(it) }
                    dialogLoader?.hideProgressDialog()
                }
                }

            }

            override fun onFailure(call: Call<UserAccountResponse>, t: Throwable) {
                t.message?.let { binding.root.snackbar(it) }
                dialogLoader?.hideProgressDialog()

            }
        })

    }

    private fun allowEditable(editTextView: TextInputEditText){
        editTextView.isCursorVisible = true;
        editTextView.isFocusableInTouchMode = true;
        editTextView.inputType = InputType.TYPE_CLASS_TEXT;
        editTextView.requestFocus(); //to trigger the soft input
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

    @SuppressWarnings("deprecation")
    private fun getRealPathFromURI(context: Context, uri: Uri?): String? {
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