package ru.itmo.sd.homework4.exchange.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.sd.homework4.exchange.model.Company
import ru.itmo.sd.homework4.exchange.service.ExchangeService

@RestController
@RequestMapping("/exchange")
class ExchangeController(
    private val exchangeService: ExchangeService
) {
    @GetMapping("/{company}")
    fun companyByName(
        @PathVariable("company") company: String
    ): Company = exchangeService.findCompanyByName(company)

    @GetMapping("/new-company")
    fun newCompany(
        @RequestParam("name") company: String,
        @RequestParam("shares") shares: Long,
        @RequestParam("cost") cost: Double
    ): String {
        exchangeService.newCompany(company, shares, cost)
        return "Successfully added company $company with $shares shares at the cost of $cost\$"
    }

    @GetMapping("/remove-all")
    fun removeAllCompanies(): String {
        exchangeService.removeAllCompanies()
        return "All companies have been successfully removed"
    }

    @GetMapping("/update-cost")
    fun updateShareCost(
        @RequestParam("company") company: String,
        @RequestParam("new-cost") newCost: Double
    ): String {
        exchangeService.updateShareCost(company, newCost)
        return "Successfully updated cost of $company shares"
    }

    @GetMapping("/update-shares-amount")
    fun updateSharesAmount(
        @RequestParam("company") company: String,
        @RequestParam("new-amount") newAmount: Long
    ) = exchangeService.updateSharesAmount(company, newAmount)

    @GetMapping("/add-shares")
    fun addShares(
        @RequestParam("company") company: String,
        @RequestParam("amount") amount: Long
    ): String {
        exchangeService.addShares(company, amount)
        return "Successfully added $amount $company shares"
    }

    @GetMapping("/remove-shares")
    fun removeShares(
        @RequestParam("company") company: String,
        @RequestParam("amount") amount: Long
    ): String {
        exchangeService.removeShares(company, amount)
        return "Successfully removed $amount $company shares"
    }
}