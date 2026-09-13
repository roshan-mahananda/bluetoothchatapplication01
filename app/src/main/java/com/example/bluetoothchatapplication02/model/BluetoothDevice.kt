package com.example.bluetoothchatapplication02.model

data class BluetoothDevice(
    val deviceAddress: String,
    val deviceName: String,
    var userAlias: String? = null
)
