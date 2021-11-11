package com.example.flashsports.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.example.flashsports.R

/**
 * DialogLoader will be used to show and hide Dialog with ProgressBar
 */
class DialogLoader(private val context: Context) {
    private var alertDialog: AlertDialog? = null
    private var dialog: AlertDialog.Builder? = null
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        initDialog()
    }

    private fun initDialog() {
        dialog = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.layout_dialog_loader, null)
        dialog!!.setView(dialogView)
        dialog!!.setCancelable(false)
        alertDialog = dialog!!.create()
        alertDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showProgressDialog() {
        try {
            alertDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideProgressDialog() {
        try {
            alertDialog!!.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}