package com.freevpn.app

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object VpnGateRepository {
    private const val API = "https://www.vpngate.net/api/iphone/"

    suspend fun fetch(): List<VpnServer> = withContext(Dispatchers.IO) {
        val conn = URL(API).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        try {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            text.lineSequence()
                .filter { it.isNotBlank() && !it.startsWith("*") && !it.startsWith("#") }
                .mapNotNull(::parse)
                .sortedByDescending { it.score }
                .toList()
        } finally {
            conn.disconnect()
        }
    }

    // الأعمدة: HostName,IP,Score,Ping,Speed,CountryLong,CountryShort,NumVpnSessions,...,OpenVPN_ConfigData_Base64
    private fun parse(line: String): VpnServer? = try {
        val p = line.split(",")
        if (p.size < 15) null else {
            val config = String(Base64.decode(p.last().trim(), Base64.DEFAULT))
            VpnServer(
                host = p[0], ip = p[1],
                score = p[2].toLongOrNull() ?: 0,
                pingMs = p[3].toIntOrNull() ?: 9999,
                speedMbps = (p[4].toDoubleOrNull() ?: 0.0) / 1_000_000.0,
                country = p[5], countryCode = p[6],
                sessions = p[7].toIntOrNull() ?: 0,
                ovpnConfig = config
            )
        }
    } catch (e: Exception) { null }
}
