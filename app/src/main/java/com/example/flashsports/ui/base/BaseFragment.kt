package com.example.flashsports.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.flashsports.data.preferences.UserPreferences
import com.example.flashsports.singleton.dataStore
import com.example.flashsports.ui.viewModels.FilesViewModel

abstract class BaseFragment<dataBinding : ViewDataBinding> : Fragment() {

  lateinit var viewModel: ViewModel
  protected lateinit var binding: dataBinding
  protected lateinit var navController: NavController
  protected lateinit var userPreferences: UserPreferences

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    userPreferences = UserPreferences(requireContext().dataStore)
    binding = getFragmentBinding(inflater, container)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      lifecycleOwner = this@BaseFragment
    }
    navController = findNavController()
    setupTheme()
    setupClickListeners()
  }

  abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): dataBinding

  abstract fun setupTheme()

  abstract fun setupClickListeners()


}