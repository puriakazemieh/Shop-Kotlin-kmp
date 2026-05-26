package com.kazemieh.data.profile.source

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*




interface AddressDataSource {
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
