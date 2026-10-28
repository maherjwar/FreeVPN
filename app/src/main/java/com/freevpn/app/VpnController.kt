package com.freevpn.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.IOpenVPNServiceInternal
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.StringReader

/** غلاف بسيط فوق مكتبة ics-openvpn. قد تحتاج أسماء الدوال لتعديل بسيط حسب إصدار المكتبة. */
class VpnController(private val ctx: Context) : VpnStatus.StateListener {

    private val _status = MutableStateFlow(ConnectionStatus.LEVEL_NOTCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { VpnStatus.addStateListener(this) }

    fun connect(server: VpnServer) {
        try {
            _error.value = null
            val parser = ConfigParser()
            parser.parseConfig(StringReader(server.ovpnConfig))
            val profile: VpnProfile = parser.convertProfile()
            profile.mName = "${server.country} - ${server.ip}"
            ProfileManager.setTemporaryProfile(ctx, profile)
            VPNLaunchHelper.startOpenVpn(profile, ctx, "FreeVPN", true)
        } catch (e: Exception) {
            _error.value = "فشل تجهيز الاتصال: ${e.message}"
        }
    }

    fun disconnect() {
        val intent = Intent(ctx, OpenVPNService::class.java).apply {
            action = OpenVPNService.START_SERVICE
        }
        ctx.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    IOpenVPNServiceInternal.Stub.asInterface(binder).stopVPN(false)
                } catch (_: Exception) {}
                ctx.unbindService(this)
            }
            override fun onServiceDisconnected(name: ComponentName?) {}
        }, Context.BIND_AUTO_CREATE)
    }

    override fun updateState(
        state: String?, logmessage: String?, localizedResId: Int,
        level: ConnectionStatus?, intent: Intent?
    ) { level?.let { _status.value = it } }

    override fun setConnectedVPN(uuid: String?) {}
}
