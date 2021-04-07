package ru.itmo.sd.homework4.exchange.service

import org.springframework.stereotype.Service
import ru.itmo.sd.homework4.exchange.exceptions.CompanyAlreadyExists
import ru.itmo.sd.homework4.exchange.exceptions.CompanyNotFound
import ru.itmo.sd.homework4.exchange.exceptions.InsufficientSharesAmount
import ru.itmo.sd.homework4.exchange.model.Company
import ru.itmo.sd.homework4.exchange.repository.CompanyRepository

@Service
class ExchangeService(
    private val companyRepo: CompanyRepository
) {
    fun newCompany(companyName: String, shares: Long, cost: Double) {
        if (companyRepo.existsCompanyByName(companyName)) {
            throw CompanyAlreadyExists(companyName)
        }
        val company = Company(0, companyName, shares, cost)
        companyRepo.save(company)
    }

    fun removeAllCompanies() = companyRepo.deleteAll()

    fun updateShareCost(companyName: String, newCost: Double) {
        val company = companyRepo.findByName(companyName)
            ?: throw CompanyNotFound(companyName)
        val updatedCompany = company.copy(shareCost = newCost)
        companyRepo.save(updatedCompany)
    }

    fun updateSharesAmount(companyName: String, newAmount: Long) {
        val company = companyRepo.findByName(companyName)
            ?: throw CompanyNotFound(companyName)
        val updatedCompany = company.copy(sharesAmount = newAmount)
        companyRepo.save(updatedCompany)
    }

    fun addShares(companyName: String, amount: Long) {
        val company = companyRepo.findByName(companyName)
            ?: throw CompanyNotFound(companyName)
        val updatedCompany = company.let { it.copy(sharesAmount = it.sharesAmount + amount) }
        companyRepo.save(updatedCompany)
    }

    fun removeShares(companyName: String, amount: Long) {
        val company = companyRepo.findByName(companyName)
            ?: throw CompanyNotFound(companyName)
        if (company.sharesAmount < amount) {
            throw InsufficientSharesAmount(companyName, company.sharesAmount, amount)
        }
        val updatedCompany = company.let { it.copy(sharesAmount = it.sharesAmount - amount) }
        companyRepo.save(updatedCompany)
    }

    fun findCompanyByName(name: String): Company =
        companyRepo.findByName(name) ?: throw CompanyNotFound(name)
}