package com.example.flashsports.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.flashsports.R

abstract class BaseActivity<dataBinding : ViewDataBinding> : AppCompatActivity() {

  protected lateinit var binding: dataBinding
  protected lateinit var navController: NavController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = getActivityBinding(layoutInflater)
    binding.apply {
      lifecycleOwner = this@BaseActivity
    }
    setContentView(binding.root)
    navController = findNavController(R.id.nav_host_fragment)
    setupTheme()
    setupNavigation()
  }

  abstract fun getActivityBinding(inflater: LayoutInflater): dataBinding

  abstract fun setupTheme()

  abstract fun setupNavigation()

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

}
