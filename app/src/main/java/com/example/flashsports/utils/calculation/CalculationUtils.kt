package com.example.flashsports.utils.calculation

import com.pixplicity.easyprefs.library.Prefs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CalculationUtils {

    companion object {
        fun calculateLoanDueAmount(amount: Long, interestRate: Double, processingFee: Double,duration:Int): Double = amount + ((((interestRate.toDouble() / 100)) * amount)*duration).roundToInt() + processingFee

        fun calculateLoanBalance(amountPaid:Long,loanDueAmount:Long):Long = loanDueAmount - amountPaid

        fun calculateProcessingFee(fee:Double,amount:Long):Double = ((fee / 100) * amount)

        fun calculateLoanAmount():Long {
            var amount: Long = 0
            if (Prefs.getString("credit_score").isEmpty()) {

                }else{
                var score = Prefs.getString("credit_score").toFloat()


                when (score.roundToInt()) {
                    in 0..100 -> amount = (score * 5000).toLong()
                    in 101..200 -> amount = (score * 5500).toLong()
                    in 201..300 -> amount = (score * 6000).toLong()
                    in 301..400 -> amount = (score * 6500).toLong()
                    in 401..500 -> amount = (score * 7000).toLong()
                    in 501..600 -> amount = (score * 7500).toLong()
                    in 601..700 -> amount = (score * 8000).toLong()
                    in 701..800 -> amount = (score * 8500).toLong()
                    in 801..900 -> amount = (score * 9000).toLong()
                    in 901..1000 -> amount = (score * 10000).toLong()

                }

            }
            return amount
        }

    }

}