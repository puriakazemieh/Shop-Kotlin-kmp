package com.kazemieh.network.address

import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.address.dto.request.CreateAddressRequest
import com.kazemieh.network.address.dto.request.UpdateAddressRequest
import com.kazemieh.network.address.dto.response.AddressResponse

interface AddressApi {
    suspend fun getAddresses(): List<AddressResponse>
    suspend fun getAddress(id: Long): AddressResponse
    suspend fun getDefaultAddress(): AddressResponse
    suspend fun createAddress(request: CreateAddressRequest): AddressResponse
    suspend fun updateAddress(id: Long, request: UpdateAddressRequest): AddressResponse
    suspend fun deleteAddress(id: Long)
    suspend fun setDefaultAddress(id: Long): AddressResponse
}
