package com.example.flashsports.ui.interfaces

interface FirebaseCallback {
  fun onSuccessListener()
  fun onFailureListener(e: Exception)
}