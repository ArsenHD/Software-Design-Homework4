package ru.itmo.sd.homework4.exchange.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Companies")
data class Company(
    @Id @GeneratedValue val id: Long,
    val name: String,
    val sharesAmount: Long,
    val shareCost: Double
)