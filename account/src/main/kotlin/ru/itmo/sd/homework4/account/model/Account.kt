package ru.itmo.sd.homework4.account.model

import javax.persistence.*

@Entity
@Table(name = "Accounts")
data class Account(
    @Id
    @GeneratedValue
    @Column(name = "id")
    val id: Long,
    val username: String,
    @ElementCollection(fetch = FetchType.EAGER)
    val shares: MutableMap<String, Long>,
    val balance: Double
)