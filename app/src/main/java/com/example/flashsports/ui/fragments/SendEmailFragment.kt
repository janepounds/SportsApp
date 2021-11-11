package com.example.flashsports.ui.fragments


import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.flashsports.databinding.FragmentSendEmailBinding
import com.example.flashsports.network.ApiClient
import com.example.flashsports.network.ApiRequests
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel
import com.example.flashsports.utils.DialogLoader


class SendEmailFragment :  BaseFragment<FragmentSendEmailBinding>()  {

    private val apiRequests: ApiRequests? by lazy { ApiClient.getLoanInstance() }
    private var dialogLoader: DialogLoader? = null
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSendEmailBinding.inflate(inflater, container, false)

    override fun setupTheme() {

    }

    override fun setupClickListeners() {
       binding.toolbarLayout.backBtn.setOnClickListener{requireActivity().onBackPressed()}
        binding.sendEmailButton.setOnClickListener{
            dialogLoader = context?.let { DialogLoader(it) }
            dialogLoader?.showProgressDialog()

            /***********call send email endpoint***************/


        }
    }



}