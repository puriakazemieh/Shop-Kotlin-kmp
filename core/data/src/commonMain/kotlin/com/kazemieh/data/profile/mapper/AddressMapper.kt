package com.kazemieh.data.profile.mapper

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



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
