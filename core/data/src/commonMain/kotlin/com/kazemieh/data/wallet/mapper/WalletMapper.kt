package com.kazemieh.data.wallet.mapper

import com.kazemieh.domain.wallet.*
import com.kazemieh.network.wallet.dto.response.*
import com.kazemieh.network.admin.dto.response.*

fun WalletBalanceResponse.toDomain() = WalletBalance(
    balance = balance,
    userId = userId
)

fun WalletTransactionResponse.toDomain() = WalletTransaction(
    id = id,
    amount = amount,
    type = type,
    description = description,
    referenceId = referenceId,
    createdAt = createdAt
)

fun AdminWalletUserResponse.toDomain() = AdminWalletUser(
    userId = userId,
    email = email,
    fullName = fullName,
    balance = balance
)

fun AdminWithdrawalResponse.toDomain() = AdminWithdrawal(
    id = id,
    userId = userId,
    userFullName = userFullName,
    userEmail = userEmail,
    amount = amount,
    iban = iban,
    status = status,
    adminNote = adminNote,
    createdAt = createdAt
)
