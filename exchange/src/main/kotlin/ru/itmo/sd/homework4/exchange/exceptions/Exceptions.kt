package ru.itmo.sd.homework4.exchange.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CompanyNotFound(
    companyName: String
) : RuntimeException("Company $companyName not found")

@ResponseStatus(HttpStatus.CONFLICT)
class CompanyAlreadyExists(
    companyName: String
) : RuntimeException("Company $companyName already exists")

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class InsufficientSharesAmount(
    companyName: String,
    present: Long,
    needed: Long
) : RuntimeException("Insufficient amount of $companyName shares: present $present, needed $needed")
