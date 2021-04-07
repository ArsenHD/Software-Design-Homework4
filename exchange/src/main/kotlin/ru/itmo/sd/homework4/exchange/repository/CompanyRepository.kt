package ru.itmo.sd.homework4.exchange.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.itmo.sd.homework4.exchange.model.Company

@Repository
interface CompanyRepository : CrudRepository<Company, Long> {
    fun findByName(name: String): Company?
    fun existsCompanyByName(name: String): Boolean
}
