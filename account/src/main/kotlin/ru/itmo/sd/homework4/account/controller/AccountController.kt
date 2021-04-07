package ru.itmo.sd.homework4.account.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import ru.itmo.sd.homework4.account.exceptions.CompanyNotFound
import ru.itmo.sd.homework4.account.model.AccountInfo
import ru.itmo.sd.homework4.account.model.AccountInfoForCompany
import ru.itmo.sd.homework4.account.model.Company
import ru.itmo.sd.homework4.account.service.AccountService

@RestController
@RequestMapping("/account")
class AccountController @Autowired constructor(
    private val accountService: AccountService,
    private val restTemplate: RestTemplate
) {
    private val exchangeUrl = "http://localhost:2222/exchange"

    @GetMapping
    fun enterAccount(
        @RequestParam("username") username: String
    ): AccountInfo {
        val account = accountService.enterAccount(username)
        val companiesInfo = account.shares.map { (companyName, amount) ->
            val company = restTemplate.getForObject("$exchangeUrl/$companyName", Company::class.java)
                ?: throw CompanyNotFound(companyName)
            val cost = amount * company.shareCost
            AccountInfoForCompany(companyName, amount, cost)
        }
        return AccountInfo(companiesInfo)
    }

    @GetMapping("/remove-all")
    fun removeAllAccounts(): String {
        accountService.removeAllAccounts()
        return "All accounts have been successfully removed"
    }

    @GetMapping("/new")
    fun newAccount(
        @RequestParam("username") username: String
    ): String {
        accountService.newAccount(username)
        return "User $username has been successfully registered"
    }

    @GetMapping("/deposit")
    fun deposit(
        @RequestParam("username") username: String,
        @RequestParam("sum") sum: Double
    ): String {
        accountService.deposit(username, sum)
        return "Sum of $sum$ has been successfully deposited to $username"
    }

    @GetMapping("/total_balance")
    fun totalBalance(
        @RequestParam("username") username: String,
    ): String {
        val balance = accountService.totalBalance(username)
        return "Total balance of account $username is: ${balance}$"
    }

    @GetMapping("/buy")
    fun buyShares(
        @RequestParam("username") username: String,
        @RequestParam("company") company: String,
        @RequestParam("amount") amount: Long
    ): String {
        accountService.buyShares(username, company, amount)
        return "Successfully bought $amount $company shares"
    }

    @GetMapping("/sell")
    fun sellShares(
        @RequestParam("username") username: String,
        @RequestParam("company") company: String,
        @RequestParam("amount") amount: Long
    ): String {
        accountService.sellShares(username, company, amount)
        return "Successfully sold $amount $company shares"
    }
}