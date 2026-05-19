package com.kazemieh.data.profile.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.profile.source.AddressDataSource
import com.kazemieh.domain.model.Address
import com.kazemieh.domain.repository.AddressRepository

class AddressRepositoryImpl(
    private val addressDataSource: AddressDataSource
) : AddressRepository {

    override suspend fun getAddresses(): AppResult<List<Address>> =
        addressDataSource.getAddresses()

    override suspend fun getAddress(id: Long): AppResult<Address> =
        addressDataSource.getAddress(id)

    override suspend fun getDefaultAddress(): AppResult<Address> =
        addressDataSource.getDefaultAddress()

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
    ): AppResult<Address> = addressDataSource.createAddress(
        receiverName,
        receiverPhone,
        country,
        province,
        city,
        addressLine1,
        addressLine2,
        postalCode,
        setAsDefault
    )

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
    ): AppResult<Address> = addressDataSource.updateAddress(
        id,
        receiverName,
        receiverPhone,
        country,
        province,
        city,
        addressLine1,
        addressLine2,
        postalCode
    )

    override suspend fun deleteAddress(id: Long): AppResult<Unit> =
        addressDataSource.deleteAddress(id)

    override suspend fun setDefaultAddress(id: Long): AppResult<Address> =
        addressDataSource.setDefaultAddress(id)
}
