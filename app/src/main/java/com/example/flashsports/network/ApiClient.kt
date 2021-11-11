package com.example.flashsports.network

import android.util.Log
import com.example.flashsports.constants.Constants
import com.example.flashsports.data.config.Config
import com.example.flashsports.utils.generateRequestId
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * APIClient handles all the Network API Requests using Retrofit Library
 **/
object ApiClient {
    private var apiRequests: ApiRequests? = null

    fun getLoanInstance(): ApiRequests? {
        return if (apiRequests == null) {
            val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.tag("Retrofit2 Errors").e("message: $message")

            }
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            var request_id: String = generateRequestId()

            var apiInterceptor: ApiInterceptor = ApiInterceptor.Builder()
                .consumerKey(Config.LOAN_API_KEY)
                .requestId(request_id)
                .build()

            val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS) //.addInterceptor(apiInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(Interceptor { chain ->
                    val request =
                        chain.request().newBuilder() // .addHeader(Constant.Header, authToken)
                            .build()
                    chain.proceed(request)
                })
                .addInterceptor(apiInterceptor)
                .build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.LOAN_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiRequests = retrofit.create(ApiRequests::class.java)
            apiRequests
        } else {
            apiRequests
        }
    }


}