package com.kazemieh.network.common

import io.ktor.http.Url

/**
 * اعتبارسنجی مبدأ (Origin) و آدرس‌های ورودی override در URL یا پیکربندی زمان اجرا.
 * طبق الزام P01-SECURITY-CODE-006، در محیط‌های release نباید اجازه داد با تزریق پارامترهایی مثل `?api=`
 * یا `?wpApi=`، ترافیک به سمت localhost، آدرس‌های خصوصی (private IP/RFC 1918) یا دامنه‌های غیرمجاز (foreign) هدایت شود.
 */
object ApiOriginValidator {

    private val IPV4_REGEX = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")
    private val PRIVATE_DOMAIN_SUFFIXES = listOf(".local", ".internal", ".lan", ".home.arpa", ".corp")

    fun isLocalhost(host: String): Boolean {
        val normalized = host.trim().lowercase().removeSurrounding("[", "]")
        return normalized == "localhost" ||
            normalized == "127.0.0.1" ||
            normalized == "::1" ||
            normalized.endsWith(".localhost")
    }

    fun isPrivateAddress(host: String): Boolean {
        val normalized = host.trim().lowercase().removeSurrounding("[", "]")
        
        // Loopback is also private
        if (isLocalhost(normalized)) return true

        val match = IPV4_REGEX.matchEntire(normalized)
        if (match != null) {
            val (o1Str, o2Str, o3Str, o4Str) = match.destructured
            val o1 = o1Str.toIntOrNull() ?: return true
            val o2 = o2Str.toIntOrNull() ?: return true
            val o3 = o3Str.toIntOrNull() ?: return true
            val o4 = o4Str.toIntOrNull() ?: return true

            if (o1 !in 0..255 || o2 !in 0..255 || o3 !in 0..255 || o4 !in 0..255) return true

            // 0.0.0.0/8
            if (o1 == 0) return true
            // 10.0.0.0/8
            if (o1 == 10) return true
            // 127.0.0.0/8
            if (o1 == 127) return true
            // 172.16.0.0/12
            if (o1 == 172 && o2 in 16..31) return true
            // 192.168.0.0/16
            if (o1 == 192 && o2 == 168) return true
            // 169.254.0.0/16
            if (o1 == 169 && o2 == 254) return true
        }

        // Check private IPv6 unique local (fc00::/7) or link-local (fe80::/10)
        if (normalized.startsWith("fc") || normalized.startsWith("fd") || normalized.startsWith("fe8") || normalized.startsWith("fe9") || normalized.startsWith("fea") || normalized.startsWith("feb")) {
            if (normalized.contains(':')) return true
        }

        // Check private domain suffixes or unqualified hostnames without dots
        if (PRIVATE_DOMAIN_SUFFIXES.any { normalized.endsWith(it) }) return true
        if (!normalized.contains('.') && !normalized.contains(':')) return true

        return false
    }

    fun isForeignHost(host: String, allowedHosts: Set<String>): Boolean {
        val normalized = host.trim().lowercase().removeSurrounding("[", "]")
        val normalizedAllowed = allowedHosts.map { it.trim().lowercase().removeSurrounding("[", "]") }
        return !normalizedAllowed.contains(normalized)
    }

    /**
     * بررسی و اعتبارسنجی candidate URL جهت جایگزینی آدرس پایه API.
     * @param candidateUrl آدرس پیشنهادی برای override
     * @param allowedHosts فهرست هاست‌های مجاز طبق backendProfile
     * @param isRelease آیا در حالت release/production اجرا می‌شود؟
     * @return در صورت معتبر بودن، همان URL برگردانده می‌شود؛ در غیر این صورت null.
     */
    fun validateOriginOverride(
        candidateUrl: String?,
        allowedHosts: Set<String>,
        isRelease: Boolean
    ): String? {
        if (candidateUrl.isNullOrBlank()) return null
        
        val url = runCatching { Url(candidateUrl.trim()) }.getOrNull() ?: return null
        val protocol = url.protocol.name.lowercase()
        val host = url.host.trim().lowercase().removeSurrounding("[", "]")

        if (isRelease) {
            // در release فقط HTTPS مجاز است
            if (protocol != "https") return null
            // در release هاست‌های لوکال هاست یا شبکه خصوصی غیرمجازند
            if (isLocalhost(host)) return null
            if (isPrivateAddress(host)) return null
            // در release هاست باید صریحاً در allowedHosts باشد
            if (isForeignHost(host, allowedHosts)) return null
        } else {
            // در debug/dev پروتکل http یا https مجاز است
            if (protocol != "http" && protocol != "https") return null
            // اگر در allowedHosts نباشد، فقط لوکال یا شبیه‌ساز (10.0.2.2) در حالت دیباگ مجاز است
            val isAllowed = !isForeignHost(host, allowedHosts)
            val isLocalDev = isLocalhost(host) || host == "10.0.2.2"
            if (!isAllowed && !isLocalDev) return null
        }

        return candidateUrl.trim()
    }
}
