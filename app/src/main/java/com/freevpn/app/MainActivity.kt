package com.freevpn.app

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.blinkt.openvpn.core.ConnectionStatus

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen(vm) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(vm: MainViewModel) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val servers by vm.servers.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val loadError by vm.loadError.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val country by vm.country.collectAsStateWithLifecycle()
    val status by vm.controller.status.collectAsStateWithLifecycle()
    val connError by vm.controller.error.collectAsStateWithLifecycle()

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) vm.connect()
    }

    val isConnected = status == ConnectionStatus.LEVEL_CONNECTED
    val isBusy = status == ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED ||
        status == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET ||
        status == ConnectionStatus.LEVEL_START

    val statusText = when {
        isConnected -> "متصل"
        isBusy -> "جارٍ الاتصال…"
        else -> "غير متصل"
    }

    val countries = remember(servers) { servers.map { it.country }.distinct().sorted() }
    val visible = remember(servers, country) { servers.filter { country == null || it.country == country } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(topBar = { TopAppBar(title = { Text("FreeVPN") }) }) { pad ->
            Column(Modifier.padding(pad).padding(16.dp)) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(statusText, fontSize = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(selected?.let { "${it.flag} ${it.country} · ${it.ip}" } ?: "اختر سيرفرًا")
                        Spacer(Modifier.height(12.dp))
                        Button(
                            enabled = selected != null,
                            onClick = {
                                if (isConnected || isBusy) vm.disconnect()
                                else {
                                    val intent = VpnService.prepare(ctx)
                                    if (intent != null) permission.launch(intent) else vm.connect()
                                }
                            }
                        ) { Text(if (isConnected || isBusy) "قطع الاتصال" else "اتصال") }
                        (connError ?: loadError)?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("الدول", Modifier.weight(1f), fontSize = 16.sp)
                    TextButton(onClick = vm::refresh) { Text(if (loading) "جارٍ التحديث…" else "تحديث") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(country == null, { vm.filterCountry(null) }, { Text("الكل") }) }
                    items(countries) { c ->
                        FilterChip(country == c, { vm.filterCountry(c) }, { Text(c) })
                    }
                }

                Spacer(Modifier.height(8.dp))
                if (loading && servers.isEmpty()) CircularProgressIndicator()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(visible, key = { it.ip + it.host }) { s ->
                        val sel = s == selected
                        Card(
                            Modifier.fillMaxWidth().clickable { vm.select(s) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(s.flag, fontSize = 24.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(s.country)
                                    Text("${s.ip} · ${s.sessions} مستخدم", fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${s.pingMs} ms", fontSize = 12.sp)
                                    Text("%.0f Mbps".format(s.speedMbps), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
