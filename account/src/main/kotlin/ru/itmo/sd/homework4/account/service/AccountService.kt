package ru.itmo.sd.homework4.account.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.itmo.sd.homework4.account.exceptions.*
import ru.itmo.sd.homework4.account.model.Account
import ru.itmo.sd.homework4.account.model.Company
import ru.itmo.sd.homework4.account.repository.AccountRepository

@Service
class AccountService @Autowired constructor(
    private val accountRepo: AccountRepository,
    private val restTemplate: RestTemplate
) {
    private val exchangeUrl = "http://localhost:2222/exchange"

    fun enterAccount(username: String): Account =
        accountRepo.findByUsername(username)
            ?: throw AccountNotFound(username)

    fun removeAllAccounts() = accountRepo.deleteAll()

    fun newAccount(username: String) {
        if (accountRepo.existsAccountByUsername(username)) {
            throw AccountAlreadyExists(username)
        }
        val account = Account(0, username, mutableMapOf(), 0.0)
        accountRepo.save(account)
    }

    fun deposit(username: String, sum: Double) {
        val account = accountRepo.findByUsername(username)
            ?: throw AccountNotFound(username)
        val updatedAccount = account.let { it.copy(balance = it.balance + sum) }
        accountRepo.save(updatedAccount)
    }

    fun totalBalance(username: String): Double {
        val account = accountRepo.findByUsername(username)
            ?: throw AccountNotFound(username)
        val balance = account.balance
        val sharesTotalCost = account.shares
            .toList()
            .sumByDouble { (companyName, amount) ->
                val company = restTemplate.getForObject("$exchangeUrl/$companyName", Company::class.java)
                    ?: throw CompanyNotFound(companyName)
                company.shareCost * amount
            }
        return balance + sharesTotalCost
    }

    fun buyShares(username: String, companyName: String, amount: Long) {
        val account = accountRepo.findByUsername(username)
            ?: throw AccountNotFound(username)
        val company = restTemplate.getForObject("$exchangeUrl/$companyName", Company::class.java)
            ?: throw CompanyNotFound(companyName)
        val cost = company.shareCost * amount

        if (account.balance < cost) {
            throw InsufficientBalance(username, account.balance, cost)
        }

        if (company.sharesAmount < amount) {
            throw InsufficientSharesAmount(companyName, company.sharesAmount, amount)
        }

        val updatedAccount = account.let {
            it.shares.merge(companyName, amount, Long::plus)
            it.copy(balance = it.balance - cost)
        }

        accountRepo.save(updatedAccount)
        restTemplate.getForObject(
            "$exchangeUrl/update-shares-amount?company=$companyName&new-amount=${company.sharesAmount - amount}",
            Unit::class.java
        )
    }

    fun sellShares(username: String, companyName: String, amount: Long) {
        val account = accountRepo.findByUsername(username)
            ?: throw AccountNotFound(username)
        val company = restTemplate.getForObject("$exchangeUrl/$companyName", Company::class.java)
            ?: throw CompanyNotFound(companyName)
        val cost = company.shareCost * amount
        val currentAmount = account.shares.getOrDefault(companyName, 0)

        require(amount != 0L) { "Cannot sell 0 shares" }

        if (currentAmount < amount) {
            throw InsufficientSharesAmount(companyName, currentAmount, amount)
        }

        val updatedAccount = account.let {
            it.shares[companyName] = currentAmount - amount
            it.copy(balance = it.balance + cost)
        }

        accountRepo.save(updatedAccount)
        restTemplate.getForObject(
            "$exchangeUrl/update-shares-amount?company=$companyName&new-amount=${company.sharesAmount + amount}",
            Unit::class.java
        )
    }
}