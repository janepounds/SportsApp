package com.example.flashsports.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flashsports.R
import com.example.flashsports.databinding.FragmentChangePinBinding
import com.example.flashsports.databinding.FragmentFAQsBinding
import com.example.flashsports.ui.base.BaseFragment
import com.example.flashsports.ui.viewModels.FilesViewModel

class FAQsFragment : BaseFragment<FragmentFAQsBinding>() {
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentFAQsBinding.inflate(inflater, container, false)

    override fun setupTheme() {

    }

    override fun setupClickListeners() {
        binding.toolbarLayout.backBtn.setOnClickListener { requireActivity().onBackPressed() }

        binding.layoutFaq1.setOnClickListener {
            //FIRST QUESTION
            if (binding.cardviewFaqAnswer1.visibility == View.GONE) {
                binding.cardviewFaqAnswer1.visibility = View.VISIBLE
                binding.cardviewFaqAnswer2.visibility = View.GONE
                binding.cardviewFaqAnswer3.visibility = View.GONE
                binding.cardviewFaqAnswer4.visibility = View.GONE
                binding.cardviewFaqAnswer5.visibility = View.GONE
                binding.cardviewFaqAnswer6.visibility = View.GONE

                binding.rightArrowFaq.rotation = 90F
                binding.rightArrowFaq2.rotation = 0F
                binding.rightArrowFaq3.rotation = 0F
                binding.rightArrowFaq4.rotation = 0F
                binding.rightArrowFaq5.rotation = 0F
                binding.rightArrowFaq6.rotation = 0F
            } else if (binding.cardviewFaqAnswer1.visibility == View.VISIBLE) {

                binding.cardviewFaqAnswer1.visibility = View.GONE
                binding.rightArrowFaq.rotation = 0F
            }
        }

        //SECOND QUESTION
        binding.layoutFaq2.setOnClickListener {

            if (binding.cardviewFaqAnswer2.visibility == View.GONE) {
            binding.cardviewFaqAnswer1.visibility = View.GONE
                binding.cardviewFaqAnswer2.visibility = View.VISIBLE
                binding.cardviewFaqAnswer3.visibility = View.GONE
                binding.cardviewFaqAnswer4.visibility = View.GONE
                binding.cardviewFaqAnswer5.visibility = View.GONE
                binding.cardviewFaqAnswer6.visibility = View.GONE

                binding.rightArrowFaq.rotation = 0F
                binding.rightArrowFaq2.rotation = 90F
                binding.rightArrowFaq3.rotation = 0F
                binding.rightArrowFaq4.rotation = 0F
                binding.rightArrowFaq5.rotation = 0F
                binding.rightArrowFaq6.rotation = 0F
            } else if (binding.cardviewFaqAnswer2.visibility == View.VISIBLE) {

                binding.cardviewFaqAnswer2.visibility = View.GONE
                binding.rightArrowFaq2.rotation = 0F
            }
        }
            //THREE QUESTION
        binding.layoutFaq3.setOnClickListener {
            if (binding.cardviewFaqAnswer3.visibility == View.GONE) {
            binding.cardviewFaqAnswer1.visibility = View.GONE
                binding.cardviewFaqAnswer2.visibility = View.GONE
                binding.cardviewFaqAnswer3.visibility = View.VISIBLE
                binding.cardviewFaqAnswer4.visibility = View.GONE
                binding.cardviewFaqAnswer5.visibility = View.GONE
                binding.cardviewFaqAnswer6.visibility = View.GONE

                binding.rightArrowFaq.rotation = 0F
                binding.rightArrowFaq2.rotation = 0F
                binding.rightArrowFaq3.rotation = 90F
                binding.rightArrowFaq4.rotation = 0F
                binding.rightArrowFaq5.rotation = 0F
            binding.rightArrowFaq6.rotation = 0F
        } else if (binding.cardviewFaqAnswer3.visibility == View.VISIBLE) {

            binding.cardviewFaqAnswer3.visibility = View.GONE
            binding.rightArrowFaq3.rotation = 0F
        }
        }
            //FOUR QUESTION
        binding.layoutFaq4.setOnClickListener {
           if (binding.cardviewFaqAnswer4.visibility == View.GONE) {
            binding.cardviewFaqAnswer1.visibility = View.GONE
               binding.cardviewFaqAnswer2.visibility = View.GONE
               binding.cardviewFaqAnswer3.visibility = View.GONE
               binding.cardviewFaqAnswer4.visibility = View.VISIBLE
               binding.cardviewFaqAnswer5.visibility = View.GONE
               binding.cardviewFaqAnswer6.visibility = View.GONE

               binding.rightArrowFaq.rotation = 0F
               binding.rightArrowFaq2.rotation = 0F
               binding.rightArrowFaq3.rotation = 0F
               binding.rightArrowFaq4.rotation = 90F
               binding.rightArrowFaq5.rotation = 0F
               binding.rightArrowFaq6.rotation = 0F
           } else if (binding.cardviewFaqAnswer4.visibility == View.VISIBLE) {

            binding.cardviewFaqAnswer4.visibility = View.GONE
            binding.rightArrowFaq4.rotation = 0F
           }
        }
            //FIVE QUESTION
        binding.layoutFaq5.setOnClickListener {
            if (binding.cardviewFaqAnswer5.visibility == View.GONE) {
                binding.cardviewFaqAnswer1.visibility = View.GONE
                binding.cardviewFaqAnswer2.visibility = View.GONE
                binding.cardviewFaqAnswer3.visibility = View.GONE
                binding.cardviewFaqAnswer4.visibility = View.GONE
                binding.cardviewFaqAnswer5.visibility = View.VISIBLE
                binding.cardviewFaqAnswer6.visibility = View.GONE

                binding.rightArrowFaq.rotation = 0F
                binding.rightArrowFaq2.rotation = 0F
                binding.rightArrowFaq3.rotation = 0F
                binding.rightArrowFaq4.rotation = 0F
                binding.rightArrowFaq5.rotation = 90F
                binding.rightArrowFaq6.rotation = 0F
            } else if (binding.cardviewFaqAnswer5.visibility == View.VISIBLE) {

                binding.cardviewFaqAnswer5.visibility = View.GONE
                binding.rightArrowFaq5.rotation = 0F
            }
        }

            //SIX QUESTION
        binding.layoutFaq6.setOnClickListener {
            if (binding.cardviewFaqAnswer6.visibility == View.GONE) {
                binding.cardviewFaqAnswer1.visibility = View.GONE
                binding.cardviewFaqAnswer2.visibility = View.GONE
                binding.cardviewFaqAnswer3.visibility = View.GONE
                binding.cardviewFaqAnswer4.visibility = View.GONE
                binding.cardviewFaqAnswer5.visibility = View.GONE
                binding.cardviewFaqAnswer6.visibility = View.VISIBLE

                binding.rightArrowFaq.rotation = 0F
                binding.rightArrowFaq2.rotation = 0F
                binding.rightArrowFaq3.rotation = 0F
                binding.rightArrowFaq4.rotation = 0F
                binding.rightArrowFaq5.rotation = 0F
                binding.rightArrowFaq6.rotation = 90F
            }
            else if(binding.cardviewFaqAnswer6.visibility == View.VISIBLE){

                binding.cardviewFaqAnswer6.visibility = View.GONE
                binding.rightArrowFaq6.rotation = 0F
            }

        }

    }


}