package com.sap.codelab.location

/** Pure domain rules, independently testable without Android or Play services. */
internal object GeoRules {
    const val RADIUS_METERS = 200f
    const val MAX_ACTIVE_GEOFENCES = 100 // Google Play services per-app limit
    private const val PREFIX = "memo:"

    fun validPoint(latitude: Double?, longitude: Double?): Boolean =
        latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0

    fun requestId(memoId: Long): String = "$PREFIX$memoId"

    fun memoId(requestId: String): Long? =
        if (requestId.startsWith(PREFIX)) requestId.removePrefix(PREFIX).toLongOrNull()?.takeIf { it > 0 } else null
}
