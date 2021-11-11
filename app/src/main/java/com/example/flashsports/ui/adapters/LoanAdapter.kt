package com.example.flashsports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flashsports.R
import com.example.flashsports.data.models.Loan
import com.example.flashsports.databinding.ItemLoanBinding
import com.example.flashsports.singleton.MyApplication

class LoanAdapter(private val listener: LoanListener) : RecyclerView.Adapter<LoanAdapter.MyViewHolder>() {

    private val items: ArrayList<Loan> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(ItemLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewBinding.apply {
            loan = items[position]
            loanAmt.text = String.format(root.context.getString(R.string.amt_ugx), MyApplication.getNumberFormattedString(loan!!.amt))
            root.setOnClickListener { listener.onViewClick(loan!!) }
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(updated: List<Loan>) {
        items.clear()
        items.addAll(updated)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val viewBinding: ItemLoanBinding) : RecyclerView.ViewHolder(viewBinding.root)

    fun interface LoanListener {
        fun onViewClick(loan: Loan)
    }

}