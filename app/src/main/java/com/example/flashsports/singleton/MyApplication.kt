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
import androidx.core.app.NotificationManagerCompat

import com.freshchat.consumer.sdk.FreshchatNotificationConfig

import android.R
import android.net.Uri
import com.example.flashsports.data.config.Config

import com.freshchat.consumer.sdk.FreshchatConfig
import com.freshchat.consumer.sdk.Freshchat







@HiltAndroidApp
class MyApplication : Application() {
  private var freshchat: Freshchat? = null

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
    initialiseFreshChat()
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

  private fun initialiseFreshChat(){

    val freshChatConfig = FreshchatConfig(Config.FRESHDESK_APP_ID, Config.FRESHDESK_API_KEY)
    freshChatConfig.domain =Config.FRESHDESK_DOMAIN
    freshChatConfig.isCameraCaptureEnabled = true
    freshChatConfig.isGallerySelectionEnabled = true
    freshChatConfig.isResponseExpectationEnabled = true
    getFreshChatInstance(applicationContext)?.init(freshChatConfig)

    /**
     * This is the Firebase push notification server key for this sample app.
     * Please save this in your Freshchat account to test push notifications in Sample app.
     *
     * Server Key:
     * Refer Section 9.4 in integration documentation for FCM server key
     * Documentation link: https://support.freshchat.com/support/solutions/articles/50000000207
     *
     * Note: This is the push notification server key for sample app. You need to use your own server key for testing in your application
     */

    /**
     * This is the Firebase push notification server key for this sample app.
     * Please save this in your Freshchat account to test push notifications in Sample app.
     *
     * Server Key:
     * Refer Section 9.4 in integration documentation for FCM server key
     * Documentation link: https://support.freshchat.com/support/solutions/articles/50000000207
     *
     * Note: This is the push notification server key for sample app. You need to use your own server key for testing in your application
     */


  }

  private fun getFreshChatInstance(context: Context): Freshchat? {
    if (freshchat == null) {
      freshchat = Freshchat.getInstance(context)
    }
    return freshchat
  }
}