package com.example.camera.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkStatus(context: Context) : ConnectivityManager.NetworkCallback() {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkAvailableLive = MutableLiveData<Boolean>()

    fun asLiveData(): LiveData<Boolean> {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), this
        )
        return _networkAvailableLive.apply { postValue(false) }
    }

    fun dispose() = connectivityManager.unregisterNetworkCallback(this)

    override fun onAvailable(network: Network) = _networkAvailableLive.postValue(true)

    override fun onLost(network: Network) = _networkAvailableLive.postValue(false)

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) =
        _networkAvailableLive.postValue(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
}
