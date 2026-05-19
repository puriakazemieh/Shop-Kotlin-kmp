package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Address

interface AddressRepository {
    suspend fun getAddresses(): AppResult<List<Address>>
    suspend fun getAddress(id: Long): AppResult<Address>
    suspend fun getDefaultAddress(): AppResult<Address>
    suspend fun createAddress(
        receiverName: String,
        receiverPhone: String,
        country: String,
        province: String,
        city: String,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String?,
        setAsDefault: Boolean
    ): AppResult<Address>
    suspend fun updateAddress(
        id: Long,
        receiverName: String?,
        receiverPhone: String?,
        country: String?,
        province: String?,
        city: String?,
        addressLine1: String?,
        addressLine2: String?,
        postalCode: String?
    ): AppResult<Address>
    suspend fun deleteAddress(id: Long): AppResult<Unit>
    suspend fun setDefaultAddress(id: Long): AppResult<Address>
}
