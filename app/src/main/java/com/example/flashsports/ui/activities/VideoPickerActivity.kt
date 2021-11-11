package com.example.flashsports.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flashsports.R
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio

class VideoPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startPix()
    }

    private fun startPix() {
        val options = Options().apply {
            ratio = Ratio.RATIO_AUTO
            count = 1
            spanCount = 5
            flash = Flash.Auto
            mode = Mode.Video
            isFrontFacing = true
            videoDurationLimitInSeconds = 10
            path = "/"
        }

        addPixToActivity(R.id.container, options) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    val data = it.data.map { uri -> uri.toString() }.first()
                    val intent = Intent()
                    intent.putExtra("result", data)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    val intent = Intent()
                    setResult(Activity.RESULT_CANCELED, intent)
                    finish()
                }
            }
        }
    }
}