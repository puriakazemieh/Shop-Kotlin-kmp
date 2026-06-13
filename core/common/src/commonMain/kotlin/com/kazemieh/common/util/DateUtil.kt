package com.kazemieh.common.util

fun formatDateTime(isoString: String?): String {
    if (isoString == null) return ""
    // Input format: 2023-10-27T10:30:00Z
    return try {
        val parts = isoString.split("T")
        if (parts.size < 2) return isoString
        
        val datePart = parts[0] // 2023-10-27
        val timePart = parts[1].take(5) // 10:30
        
        val dateParts = datePart.split("-")
        if (dateParts.size < 3) return "$datePart $timePart"
        
        "${dateParts[0]}/${dateParts[1]}/${dateParts[2]} $timePart"
    } catch (e: Exception) {
        isoString
    }
}
