package ru.itmo.sd.homework4.account.model

data class AccountInfoForCompany(
    val companyName: String,
    val sharesAmount: Long,
    val cost: Double
)

data class AccountInfo(val companiesInfo: List<AccountInfoForCompany>)