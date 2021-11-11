package com.example.flashsports.utils

import android.app.Dialog
import android.content.ClipDescription
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.flashsports.R
import com.example.flashsports.data.config.Config
import com.example.flashsports.data.enums.EnterPinType
import com.example.flashsports.databinding.DialogWebviewFullscreenBinding
import com.example.flashsports.singleton.MyApplication


fun NavController.navigateUsingPopUp(popUpFragId: Int, destinationId: Int, args: Bundle? = null) {
    val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .setPopUpTo(popUpFragId, true)
        .build()
    navigate(destinationId, args, navOptions)
}

fun Context.hasPermissions(permission: String?): Boolean {
    return ContextCompat.checkSelfPermission(this, permission!!) == PackageManager.PERMISSION_GRANTED
}

fun spannedFromHtml(text: String): Spanned? {
    return if (Build.VERSION.SDK_INT >= 24) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(text)
    }
}

fun String.isPhoneNumberValid(): String? {
    val context = MyApplication.getAppContext()
    if (this.isEmpty()) return context.getString(R.string.phone_cannot_be_empty)
    if (this.length != 9) return context.getString(R.string.invalid_phone_number)
    return null
}

fun startAuth( navController: NavController) {
        if (navController.currentDestination!!.id !== R.id.enterPinFragment) {
            navController.popBackStack(R.id.homeFragment, false)
            navController.navigate(
                R.id.action_homeFragment_to_enterPinFragment,
                bundleOf(Config.LOGIN_TYPE to EnterPinType.LOGIN)
            )
    }
}

//    fun checkTokenExpiry(navController: NavController) {
//        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        val tokenExpiryTime =Prefs.getString("token_expiry")
//        val tokenExpiry: Date = format.parse(tokenExpiryTime)
//
//        if (Prefs.getString("token").isEmpty() || tokenExpiryTime.isEmpty() || tokenExpiry.before(Date())) {
//            navController.popBackStack(R.id.homeFragment, false)
//            navController.navigate(R.id.action_splashFragment_to_enterPinFragment,
//                bundleOf(Config.LOGIN_TYPE to EnterPinType.LOGIN)
//            )
//        }else
//            navController.navigateUsingPopUp(R.id.splashFragment, R.id.action_global_homeFragment)
//    }


fun getFirstName(name:String):String{
    val idx = name.lastIndexOf(' ')
    require(idx != -1) { "Only a single name: $name" }
    return name.substring(0, idx)

}


fun getLastName(name:String):String{
    val idx = name.lastIndexOf(' ')
    require(idx != -1) { "Only a single name: $name" }
    return name.substring(idx + 1)



}
fun showDialog(context: Context,description: String,title:String){
    val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val dialog = AlertDialog.Builder(context)
    val dialogView: View = layoutInflater.inflate(R.layout.dialog_webview_fullscreen, null)
    dialog.setView(dialogView)
    dialog.setCancelable(true)

    val dialog_button = dialogView.findViewById<ImageButton>(R.id.dialog_button)
    val dialog_title = dialogView.findViewById<TextView>(R.id.dialog_title)
    val dialog_webView = dialogView.findViewById<WebView>(R.id.dialog_webView)

    dialog_title.text = title

    val styleSheet = "<style> " +
            "body{background:#eeeeee; margin:10; padding:10} " +
            "p{color:#757575;} " +
            "img{display:inline; height:auto; max-width:100%;}" +
            "</style>"

    dialog_webView.isVerticalScrollBarEnabled = true
    dialog_webView.isHorizontalScrollBarEnabled = false
    dialog_webView.setBackgroundColor(Color.TRANSPARENT)
    dialog_webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
    dialog_webView.loadDataWithBaseURL(null, styleSheet + description, "text/html", "utf-8", null)


    val alertDialog = dialog.create()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        alertDialog.window!!.statusBarColor =
            ContextCompat.getColor(context, R.color.primaryColor)
    }

    dialog_button.setOnClickListener { alertDialog.dismiss() }

    alertDialog.show()
}

//fun DialogWebviewFullscreenBinding.showDialog(statusDialog: Dialog,description: String,context: Context){
//     val layoutInflater: LayoutInflater = LayoutInflater.from(context)
//    statusDialogBinding = DialogWebviewFullscreenBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
//    statusDialog = createFullScreenDialog(statusDialogBinding, R.drawable.slider_bg, true)
//    val styleSheet = "<style> " +
//            "body{background:#eeeeee; margin:10; padding:10} " +
//            "p{color:#757575;} " +
//            "img{display:inline; height:auto; max-width:100%;}" +
//            "</style>"
//
//    this.dialogWebView.isVerticalScrollBarEnabled = true
//    this.dialogWebView.isHorizontalScrollBarEnabled = false
//    this.dialogWebView.setBackgroundColor(Color.TRANSPARENT)
//    this.dialogWebView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
//    this.dialogWebView.loadDataWithBaseURL(
//        null,
//        styleSheet + description,
//        "text/html",
//        "utf-8",
//        null
//    )
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        statusDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        statusDialog.window!!.statusBarColor =
//            ContextCompat.getColor(context, R.color.colorPrimaryDark)
//    }
//
//    this.dialogButton.setOnClickListener { statusDialog.dismiss() }
//    statusDialog.show()
//
//}


