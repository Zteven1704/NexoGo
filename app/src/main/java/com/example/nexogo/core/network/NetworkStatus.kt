package com.example.nexogo.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitorea el estado de la conexión de red
 * Proporciona un StateFlow reactivo para observar cambios de conectividad
 */
object NetworkStatus {
    private const val TAG = "NetworkStatus"
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var connectivityManager: ConnectivityManager? = null
    
    /**
     * Inicia el monitoreo de la conexión de red
     * Debe llamarse una vez al inicio de la aplicación (Application.onCreate o MainActivity.onCreate)
     */
    fun startMonitor(context: Context) {
        try {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            // Verificar estado inicial
            checkNetworkStatus()
            
            // Crear callback para cambios de red
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "✅ Red disponible")
                    _isOnline.value = true
                }
                
                override fun onLost(network: Network) {
                    Log.d(TAG, "❌ Red perdida")
                    _isOnline.value = false
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasInternet = networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
                    
                    if (hasInternet != _isOnline.value) {
                        _isOnline.value = hasInternet
                        Log.d(TAG, if (hasInternet) "✅ Internet disponible" else "❌ Sin internet")
                    }
                }
            }
            
            // Registrar callback
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d(TAG, "✅ Monitor de red iniciado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al iniciar monitor de red: ${e.message}", e)
        }
    }
    
    /**
     * Detiene el monitoreo de la conexión de red
     */
    fun stopMonitor() {
        try {
            networkCallback?.let {
                connectivityManager?.unregisterNetworkCallback(it)
                networkCallback = null
            }
            Log.d(TAG, "✅ Monitor de red detenido")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al detener monitor de red: ${e.message}", e)
        }
    }
    
    /**
     * Verifica el estado actual de la red
     */
    private fun checkNetworkStatus() {
        try {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            
            val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            _isOnline.value = hasInternet
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al verificar estado de red: ${e.message}", e)
            _isOnline.value = false
        }
    }
}




