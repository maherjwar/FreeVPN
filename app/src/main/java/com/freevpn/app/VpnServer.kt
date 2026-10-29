package com.freevpn.app

data class VpnServer(
    val host: String,
    val ip: String,
    val score: Long,
    val pingMs: Int,
    val speedMbps: Double,
    val country: String,
    val countryCode: String,
    val sessions: Int,
    val ovpnConfig: String
) {
    val flag: String
        get() = if (countryCode.length == 2)
            countryCode.uppercase().map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
        else "🌐"
}
