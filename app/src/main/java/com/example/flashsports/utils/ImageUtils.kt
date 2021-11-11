package com.example.flashsports.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import android.graphics.BitmapFactory
import java.io.InputStream






fun ImageView.loadCircleImage(imageSrc: Any) {
    val requestOptions = RequestOptions().circleCrop()
    loadImage(imageSrc, requestOptions)
}

fun ImageView.loadImage(imageSrc: Any) {
    val requestOptions = RequestOptions().fitCenter()
    loadImage(imageSrc, requestOptions)
}

fun ImageView.loadImage(imageSrc: Any, requestOptions: RequestOptions) {
    Glide
        .with(this.context)
        .load(imageSrc)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(requestOptions)
        .into(this)
}

fun getRealPathFromUri(context: Context, contentUri: Uri?): Bitmap? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(contentUri!!)
    val bmp = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    return bmp
}

