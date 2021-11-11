package com.example.flashsports.singleton

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import com.example.flashsports.BuildConfig
import com.example.flashsports.data.models.responses.PagesDetails
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import com.example.flashsports.data.config.Config






@HiltAndroidApp
class MyApplication : Application() {

  companion object {
    private lateinit var mInstance: MyApplication
    private lateinit var numberFormat: NumberFormat
    private var staticPageDetails: List<PagesDetails> = ArrayList()
    fun getAppContext(): Context = mInstance.applicationContext
    fun getNumberFormattedString(number: Long): String = numberFormat.format(number).toString()

  }

  override fun onCreate() {
    super.onCreate()
    // Initialize the Prefs class
    Prefs.Builder()
      .setContext(this)
      .setMode(ContextWrapper.MODE_PRIVATE)
      .setPrefsName(packageName)
      .setUseDefaultSharedPreference(true)
      .build()
    mInstance = this
    numberFormat = NumberFormat.getNumberInstance(Locale.US)

    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) = super.log(priority, "ayan_$tag", message, t)

        override fun createStackElementTag(element: StackTraceElement): String =
          String.format("(%s, Line: %s, Method: %s)", super.createStackElementTag(element), element.lineNumber, element.methodName)
      })
    }
  }


}