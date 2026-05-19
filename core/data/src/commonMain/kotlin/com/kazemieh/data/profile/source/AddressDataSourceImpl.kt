package com.kazemieh.data.profile.source

import com.kazemieh.common.AppResult
import com.kazemieh.data.profile.mapper.toDomain
import com.kazemieh.domain.model.Address
import com.kazemieh.network.AddressApi
import com.kazemieh.network.dto.request.CreateAddressRequest
import com.kazemieh.network.dto.request.UpdateAddressRequest
import com.kazemieh.network.safeApiCall

class AddressDataSourceImpl(
    private val api: AddressApi
) : AddressDataSource {

    override suspend fun getAddresses(): AppResult<List<Address>> = safeApiCall {
        api.getAddresses().map { it.toDomain() }
    }

    override suspend fun getAddress(id: Long): AppResult<Address> = safeApiCall {
        api.getAddress(id).toDomain()
    }

    override suspend fun getDefaultAddress(): AppResult<Address> = safeApiCall {
        api.getDefaultAddress().toDomain()
    }

    override suspend fun createAddress(
        receiverName: String,
        receiverPhone: String,
        country: String,
        province: String,
        city: String,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String?,
        setAsDefault: Boolean
    ): AppResult<Address> = safeApiCall {
        api.createAddress(
            CreateAddressRequest(
                receiverName = receiverName,
                receiverPhone = receiverPhone,
                country = country,
                province = province,
                city = city,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                postalCode = postalCode,
                setAsDefault = setAsDefault
            )
        ).toDomain()
    }

    override suspend fun updateAddress(
        id: Long,
        receiverName: String?,
        receiverPhone: String?,
        country: String?,
        province: String?,
        city: String?,
        addressLine1: String?,
        addressLine2: String?,
        postalCode: String?
    ): AppResult<Address> = safeApiCall {
        api.updateAddress(
            id,
            UpdateAddressRequest(
                receiverName = receiverName,
                receiverPhone = receiverPhone,
                country = country,
                province = province,
                city = city,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                postalCode = postalCode
            )
        ).toDomain()
    }

    override suspend fun deleteAddress(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteAddress(id)
    }

    override suspend fun setDefaultAddress(id: Long): AppResult<Address> = safeApiCall {
        api.setDefaultAddress(id).toDomain()
    }
}
