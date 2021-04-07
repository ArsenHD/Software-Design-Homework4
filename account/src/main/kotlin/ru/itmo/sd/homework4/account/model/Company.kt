package ru.itmo.sd.homework4.account.model

data class Company(
    val id: Long,
    val name: String,
    val sharesAmount: Long,
    val shareCost: Double
)
