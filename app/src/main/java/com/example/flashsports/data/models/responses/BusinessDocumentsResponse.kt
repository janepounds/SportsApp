package com.example.flashsports.data.models.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BusinessDocumentsResponse (
    @SerializedName("status")
    @Expose
    val status:Int,
    @SerializedName("message")
    @Expose
    val message:String,
    @SerializedName("data")
    @Expose
    val data:BusinessDocumentsData?

)
class BusinessDocumentsData(
    @SerializedName("trade_license")
    @Expose
    val trade_license:String,
    @SerializedName("trade_license_thumb")
    @Expose
    val trade_license_thumb:String,
    @SerializedName("reg_certificate")
    @Expose
    val reg_certificate:String,
    @SerializedName("reg_certificate_thumb")
    @Expose
    val reg_certificate_thumb:String,
    @SerializedName("tax_reg_certificate")
    @Expose
    val tax_reg_certificate:String,
    @SerializedName("tax_reg_certificate_thumb")
    @Expose
    val tax_reg_certificate_thumb:String,
    @SerializedName("tax_clearance_certificate")
    @Expose
    val tax_clearance_certificate:String,
    @SerializedName("tax_clearance_certificate_thumb")
    @Expose
    val tax_clearance_certificate_thumb:String,
    @SerializedName("bank_statement")
    @Expose
    val bank_statement:String,
    @SerializedName("bank_statement_thumb")
    @Expose
    val bank_statement_thumb:String,
    @SerializedName("audited_financials")
    @Expose
    val audited_financials:String,
    @SerializedName("audited_financials_thumb")
    @Expose
    val audited_financials_thumb:String,
    @SerializedName("business_plan")
    @Expose
    val business_plan:String,
    @SerializedName("business_plan_thumb")
    @Expose
    val business_plan_thumb:String,
    @SerializedName("receipt_book")
    @Expose
    val receipt_book:String,
    @SerializedName("receipt_book_thumb")
    @Expose
    val receipt_book_thumb:String,
    @SerializedName("proof_of_assets")
    @Expose
    val proof_of_assets:String,
    @SerializedName("proof_of_assets_thumb")
    @Expose
    val proof_of_assets_thumb:String,
    @SerializedName("credit_reference_report")
    @Expose
    val credit_reference_report:String,
    @SerializedName("credit_reference_report_thumb")
    @Expose
    val credit_reference_report_thumb:String,
    @SerializedName("tax_report")
    @Expose
    val tax_report:String,
    @SerializedName("tax_report_thumb")
    @Expose
    val tax_report_thumb:String,
    @SerializedName("expenditure_report")
    @Expose
    val expenditure_report:String,
    @SerializedName("expenditure_report_thumb")
    @Expose
    val expenditure_report_thumb:String

)
