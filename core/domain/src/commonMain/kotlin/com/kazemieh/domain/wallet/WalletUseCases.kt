package com.kazemieh.domain.wallet

class GetWalletBalanceUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke() = repository.getBalance()
}

class GetWalletTransactionsUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(page: Int, size: Int) = repository.getTransactions(page, size)
}

class TopUpWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(amount: Double) = repository.topUp(amount)
}

class WithdrawWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(amount: Double, iban: String) = repository.withdraw(amount, iban)
}

class AdminSearchWalletUsersUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(query: String) = repository.searchWalletUsers(query)
}

class AdminAdjustWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(userId: Long, amount: Double, description: String?) =
        repository.adjustWalletBalance(userId, amount, description)
}

class AdminListWithdrawalsUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(status: String?) = repository.listWithdrawals(status)
}

class AdminProcessWithdrawalUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(id: Long, status: String, adminNote: String?) =
        repository.processWithdrawal(id, status, adminNote)
}
