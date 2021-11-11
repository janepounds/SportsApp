package com.example.flashsports.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ApiInterceptor constructor(consumerKey: kotlin.String, requestId: kotlin.String)
    : Interceptor {
    private val CONSUMER_KEY = "authorizationKey"
    private val REQUEST_ID = "request_id"

    private val consumerKey = consumerKey
    private val requestId = requestId



    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url


        val url = originalHttpUrl.newBuilder().build()

        // Request customization: add request headers

        // Request customization: add request headers
        val requestBuilder: Request.Builder = original.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader(
                CONSUMER_KEY,
                consumerKey
            )
            .addHeader(
                REQUEST_ID,
                requestId
            )

            .url(url)


        val request: Request = requestBuilder.build()
        return  chain.proceed(request)
    }

    class Builder {
        private var consumerKey: String? = null
        private var requestId: String? = null
        fun consumerKey(consumerKey: String?): Builder {
            if (consumerKey == null) throw NullPointerException("consumerKey = null")
            this.consumerKey = consumerKey
            return this
        }

        fun requestId(requestId: String?): Builder {
            if (requestId == null) throw NullPointerException("requestId = null")
            this.requestId = requestId
            return this
        }


        fun build(): ApiInterceptor {
            checkNotNull(consumerKey) { "consumerKey not set" }
            checkNotNull(requestId) { "requestId not set" }
            return ApiInterceptor(
                consumerKey!!,
                requestId!!,
            )
        }
    }
}