package ru.itmo.sd.homework4.account.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class AccountNotFound(
    username: String
) : RuntimeException("User $username not found")

@ResponseStatus(HttpStatus.CONFLICT)
class AccountAlreadyExists(
    username: String
) : RuntimeException("User $username already exists")

@ResponseStatus(HttpStatus.NOT_FOUND)
class CompanyNotFound(
    companyName: String
) : RuntimeException("Company $companyName not found")

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class InsufficientSharesAmount(
    companyName: String,
    present: Long,
    needed: Long
) : RuntimeException("Insufficient amount of $companyName shares: present $present, needed $needed")

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class InsufficientBalance(
    username: String,
    present: Double,
    needed: Double
) : RuntimeException("Insufficient balance at $username account: present $present$, needed $needed$")