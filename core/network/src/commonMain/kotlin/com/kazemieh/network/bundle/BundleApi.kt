package com.kazemieh.network.bundle

import com.kazemieh.network.bundle.dto.BundleDetailResponse
import com.kazemieh.network.bundle.dto.BundleSummaryResponse

interface BundleApi {
    suspend fun listBundles(): List<BundleSummaryResponse>
    suspend fun getBundle(slug: String): BundleDetailResponse
}
