package com.kazemieh.network.bundle

import com.kazemieh.network.bundle.dto.BundleDetailResponse
import com.kazemieh.network.bundle.dto.BundleSummaryResponse
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class BundleApiImpl(
    private val client: HttpClient
) : BundleApi {

    override suspend fun listBundles(): List<BundleSummaryResponse> = safeApiCallRaw {
        client.get("api/bundles")
    }

    override suspend fun getBundle(slug: String): BundleDetailResponse = safeApiCallRaw {
        client.get("api/bundles/$slug")
    }
}
