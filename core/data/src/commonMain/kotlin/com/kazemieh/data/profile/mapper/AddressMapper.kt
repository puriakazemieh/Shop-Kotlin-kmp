package com.kazemieh.data.profile.mapper

import com.kazemieh.domain.model.Address
import com.kazemieh.network.dto.response.AddressResponse

fun AddressResponse.toDomain(): Address {
    return Address(
        id = id,
        receiverName = receiverName,
        receiverPhone = receiverPhone,
        country = country,
        province = province,
        city = city,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        postalCode = postalCode,
        isDefault = isDefault
    )
}
