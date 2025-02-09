package com.example.ble

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.inuker.bluetooth.library.BluetoothContext
import com.inuker.bluetooth.library.BluetoothServiceImpl
import com.inuker.bluetooth.library.utils.BluetoothLog


/**
 * Created by dingjikerbo on 16/4/8.
 */
class BluetoothService : Service() {
    override fun onCreate() {
        super.onCreate()
        BluetoothLog.v(String.format("BluetoothService onCreate"))
        val context = applicationContext
        BluetoothContext.set(context)
    }

    override fun onBind(intent: Intent): IBinder? {
        BluetoothLog.v(String.format("BluetoothService onBind"))
        return BluetoothServiceImpl.getInstance()
    }
}