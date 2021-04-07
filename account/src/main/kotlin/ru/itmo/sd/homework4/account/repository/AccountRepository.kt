package ru.itmo.sd.homework4.account.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.itmo.sd.homework4.account.model.Account

@Repository
interface AccountRepository : CrudRepository<Account, Long> {
    fun findByUsername(username: String): Account?
    fun existsAccountByUsername(username: String): Boolean
}
