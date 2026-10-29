package com.freevpn.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val controller = VpnController(app)

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError

    private val _selected = MutableStateFlow<VpnServer?>(null)
    val selected: StateFlow<VpnServer?> = _selected

    private val _country = MutableStateFlow<String?>(null)
    val country: StateFlow<String?> = _country

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _loadError.value = null
            try {
                _servers.value = VpnGateRepository.fetch()
                if (_selected.value == null) _selected.value = _servers.value.firstOrNull()
            } catch (e: Exception) {
                _loadError.value = "تعذر جلب السيرفرات: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun select(s: VpnServer) { _selected.value = s }
    fun filterCountry(c: String?) { _country.value = c }
    fun connect() { _selected.value?.let(controller::connect) }
    fun disconnect() = controller.disconnect()
}
