package com.kazemieh.domain.address

import com.kazemieh.domain.address.AddressRepository

class GetAddressesUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke() = repository.getAddresses()
}

class AddAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(
        receiverName: String,
        receiverPhone: String,
        country: String,
        province: String,
        city: String,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String?,
        setAsDefault: Boolean
    ) = repository.createAddress(
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
}

class UpdateAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(
        id: Long,
        receiverName: String?,
        receiverPhone: String?,
        country: String?,
        province: String?,
        city: String?,
        addressLine1: String?,
        addressLine2: String?,
        postalCode: String?
    ) = repository.updateAddress(
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
}

class DeleteAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteAddress(id)
}

class SetDefaultAddressUseCase(private val repository: AddressRepository) {
    suspend operator fun invoke(id: Long) = repository.setDefaultAddress(id)
}
