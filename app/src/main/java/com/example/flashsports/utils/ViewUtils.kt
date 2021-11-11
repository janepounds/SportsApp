package com.example.flashsports.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.example.flashsports.R
import com.skydoves.powerspinner.PowerSpinnerView
import android.text.InputType

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.Typeface
import android.widget.DatePicker

import android.widget.EditText
import com.example.flashsports.databinding.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.flashsports.ui.fragments.businessInfo.businessDocuments.UploadBusinessDocumentsFragment
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import com.example.flashsports.singleton.MyApplication
import com.example.flashsports.utils.calculation.CalculationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL


fun View.snackbar(message: String) {
    Snackbar
        .make(this, message, Snackbar.LENGTH_LONG)
        .also { snackbar ->
            snackbar.setAction("Ok") {
                snackbar.dismiss()
            }
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).isSingleLine = false
        }.show()
}



fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun TextInputLayout.showErrorOnEditText(errorMessage: String) {
    this.isErrorEnabled = true
    this.error = errorMessage
}

fun TextInputLayout.addEndIconClickListener() {
    var isPasswordVisible = false
    this.setEndIconOnClickListener {
        if (isPasswordVisible) {
            isPasswordVisible = false
            this.editText!!.transformationMethod = AsteriskPasswordTransformationMethod()
        } else {
            isPasswordVisible = true
            this.editText!!.transformationMethod = HideReturnsTransformationMethod()
        }
    }
}

fun LayoutBusinessExpandableBinding.addToggleClickListeners(callback: () -> Unit) {
    val context = this.root.context
    this.clickToExpandLayout.setOnClickListener {
        this.expandableLayout.toggle()
        if (this.expandableLayout.isExpanded) {
            this.expandIcon.loadImage(R.drawable.ic_down_arrow)
            this.tvCollapseOrExpand.text = context.getString(R.string.click_to_collapse_tab)
        } else {
            this.expandIcon.loadImage(R.drawable.ic_next)
            this.tvCollapseOrExpand.text = context.getString(R.string.click_to_expand_tab)
        }
    }
    this.editBtn.setOnClickListener { callback() }
}


fun LayoutAccountCustomerSupportExpandableBinding.addToggleClickListeners(callback: () -> Unit) {
    val context = this.root.context
    this.clickToExpandLayout.setOnClickListener {
        this.expandableLayout.toggle()
        if (this.expandableLayout.isExpanded) {
            this.expandIcon.loadImage(R.drawable.ic_down_arrow)

        } else {
            this.expandIcon.loadImage(R.drawable.ic_next)
        }
    }
    this.tvText1.setOnClickListener { callback() }
    this.tvText2.setOnClickListener { callback() }
    this.tvText3.setOnClickListener { callback() }
}
fun PowerSpinnerView.initSpinner(lifecycleOwner: LifecycleOwner) {
    this.lifecycleOwner = lifecycleOwner
    this.setOnSpinnerOutsideTouchListener { _, _ -> this.dismiss() }
}



fun LayoutUploadDocumentBinding.updatePhotoLayout(selectedUri: Uri?) {
    if(selectedUri!=null) {
        this.uploadedPhoto = true
        this.uploadImage.loadImage(selectedUri)
    }else{
        this.uploadedPhoto = selectedUri == null
    }
}

fun LayoutUploadDocumentBinding.loadDocument(selectedUri: String?){
    if(selectedUri!=null) {
        val context = this.root.context
        this.uploadedPhoto = true

        //val link="https://emaishapayloan.api.emaisha.com/storage/images/user_business_photos/trade_license_11_1631947290.jpg"
        Glide.with(context)
            .load(selectedUri?.toUri().toString())
            .into(this.uploadImage)
        //binding.tradeLicense.title=link

        this.tvSubtitle.text = selectedUri.toString()
        this.tvSubtitle.setTypeface(null, Typeface.BOLD_ITALIC)
    }else{
        this.uploadedPhoto = selectedUri == null
    }
}

fun LayoutUploadDocumentBinding.showFile(selectedUri: String?){
    val context = this.root.context
    this.uploadedPhoto=selectedUri != null
    val path = selectedUri?.substring(selectedUri.lastIndexOf(".")+1)
    if(path.equals("jpg",ignoreCase = true)||path.equals("png",ignoreCase = true)){
        Glide.with(context)
            .load(selectedUri?.toString() )
            .into(this.uploadImage)

    }else {
        this.uploadedPhoto=selectedUri == null
        this.tvSubtitle.text = selectedUri.toString()
        this.tvSubtitle.setTypeface(null, Typeface.BOLD_ITALIC)
    }
}

fun LayoutUploadDocumentBinding.showImage(encodedImage:String){
    val context = this.root.context
    this.uploadedPhoto = encodedImage !=null
    Glide.with(context)
        .load(encodedImage)
        .into(this.uploadImage)

}
fun LayoutUploadDocumentBinding.showVideoThumbnail(bitmap: Bitmap){
    val context = this.root.context
    this.uploadedPhoto = bitmap !=null
    Glide.with(context).asBitmap()
        .load(bitmap)
        .into(this.uploadImage)
}


fun MaterialButton.enableButton() {
    this.isEnabled = true
    this.setTextColor(ContextCompat.getColor(this.context, R.color.white))
}

fun MaterialButton.disableButton() {
    this.isEnabled = false
    this.setTextColor(Color.argb(70, 255, 255, 255))
}

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}
@SuppressLint("SetTextI18n")
fun addDatePicker(ed_: EditText, context: Context?) {
    ed_.setOnClickListener { view: View? ->
        val mCurrentDate = Calendar.getInstance()
        val mYear = mCurrentDate[Calendar.YEAR]
        val mMonth = mCurrentDate[Calendar.MONTH]
        val mDay = mCurrentDate[Calendar.DAY_OF_MONTH]
        val mDatePicker = DatePickerDialog(
            context!!,
            { datePicker: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val month = selectedMonth + 1
                val formatter: NumberFormat = DecimalFormat("00")
                ed_.setText(formatter.format(
                    selectedDay.toLong()) + "-" + formatter.format(month.toLong()) + "-" + selectedYear.toString())
            },
            mDay,
            mMonth,
            mYear
        )
        mDatePicker.show()
    }
    ed_.inputType = InputType.TYPE_NULL
}






fun getHomeViewPagerHtmlText(): String = "You can now borrow up to <font color=\"#179bd7\">${String.format(MyApplication.getAppContext().getString(R.string.wallet_balance_value),MyApplication.getNumberFormattedString(
    CalculationUtils.calculateLoanAmount()))} </font> Repay in instalments of Daily, Weekly or Monthly"

fun getPayBackText(payBackAmount: String, type: String, typeAmount: String, interestRate: Double): String =
    "You will pay back <font color=\"#179bd7\">UGX $payBackAmount</font> in total with interest of ${interestRate}% and $type payment is <font color=\"#179bd7\">UGX $typeAmount</font>."

