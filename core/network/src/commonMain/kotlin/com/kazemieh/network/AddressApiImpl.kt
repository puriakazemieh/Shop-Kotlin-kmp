package com.kazemieh.network

import com.kazemieh.network.dto.request.CreateAddressRequest
import com.kazemieh.network.dto.request.UpdateAddressRequest
import com.kazemieh.network.dto.response.AddressResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AddressApiImpl(
    private val client: HttpClient
) : AddressApi {

    override suspend fun getAddresses(): List<AddressResponse> = safeApiCallRaw {
        client.get("/api/addresses")
    }

    override suspend fun getAddress(id: Long): AddressResponse = safeApiCallRaw {
        client.get("/api/addresses/$id")
    }

    override suspend fun getDefaultAddress(): AddressResponse = safeApiCallRaw {
        client.get("/api/addresses/default")
    }

    override suspend fun createAddress(request: CreateAddressRequest): AddressResponse = safeApiCallRaw {
        client.post("/api/addresses") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateAddress(id: Long, request: UpdateAddressRequest): AddressResponse = safeApiCallRaw {
        client.patch("/api/addresses/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun deleteAddress(id: Long) = safeApiCallRaw<Unit> {
        client.delete("/api/addresses/$id")
    }

    override suspend fun setDefaultAddress(id: Long): AddressResponse = safeApiCallRaw {
        client.post("/api/addresses/$id/default")
    }
}
