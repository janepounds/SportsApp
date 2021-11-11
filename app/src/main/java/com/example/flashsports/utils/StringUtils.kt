package com.example.flashsports.utils

import android.widget.ArrayAdapter
import android.widget.Spinner
import java.math.BigInteger
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun generateRequestId(): String {
    val randInt = (Random()).nextInt(10000)// Generate random integers in range 0 to 9999
    val timestamp = Timestamp(System.currentTimeMillis()).toString()
    val result = timestamp.replace("\\p{Punct}|\\s".toRegex(), "")
    val formattedRandInt = String.format("%021d", BigInteger(result + randInt))
    return "E$formattedRandInt"
}


fun selectSpinnerItemByValue(spnr: Spinner, value: String?) {
    if (value == null) return
    val adapter = spnr.adapter as ArrayAdapter<*>
    for (position in 1 until adapter.count) {
        val item = spnr.adapter.getItem(position).toString()
        if ((item.equals(value, ignoreCase = true))) {
            spnr.setSelection(position)
            return
        }
    }

}

fun getLoanRepaymentStartDate(type:String):String {
    val sdf = SimpleDateFormat("dd MMMM yyyy")
    //Getting current date
    //Getting current date
    val cal = Calendar.getInstance()
    //Displaying current date in the desired format
    //Displaying current date in the desired format

    //Date after adding the days to the current date
    //Date after adding the days to the current date
    //Displaying the new Date after addition of Days to current date
    if(type.equals("days",ignoreCase = true)){
        cal.add(Calendar.DAY_OF_MONTH, 1)

    }else if(type.equals("months",ignoreCase = true)){
        cal.add(Calendar.MONTH, 1)

    }else{
        cal.add(Calendar.WEEK_OF_YEAR, 1)

    }

    return sdf.format(cal.time)
}

fun formatDate(type:String):String{
    var date1: String? = ""
        type !=null
    try {
        val format = SimpleDateFormat("dd MMM yyyy")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date = inputFormat.parse(type)
        date1 = format.format(date)
    }catch (exception:ParseException){
        exception.printStackTrace()
    }

    return date1!!

}