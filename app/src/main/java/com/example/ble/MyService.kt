package com.example.ble

import android.Manifest
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.library.utils.BluetoothUtils

/**
 * Author: YWX
 * Date: 2021/9/7 15:11
 * Description:
 */
open class MyService : Service() {
    var mBluetoothAdapter: BluetoothAdapter? = null
    var mScanner: BluetoothLeScanner? = null
    private val isEnable = true

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initNotify()
        mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mScanner = mBluetoothAdapter!!.getBluetoothLeScanner()
        }


        Handler(Looper.getMainLooper()).postDelayed({ startScanBluetooth() }, 10000)
    }

    private fun initNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotifyO()
        } else {
            initNotify26()
        }
    }

    private fun initNotify26() {
        val mIntent = Intent(
            this,
            MainActivity::class.java
        )
        val appName = getString(R.string.app_name)
        var msgcontent = "VpSdk 启动前台服务通知！"
        msgcontent = String.format(msgcontent, appName)
        notifyApp(appName, msgcontent, mIntent, R.mipmap.ic_launcher, 0x12)
    }

    private fun notifyApp(
        msgtitle: String,
        msgcontent: String,
        mIntent: Intent,
        iconId: Int,
        nofityId: Int
    ) {
        val resultPendingIntent = PendingIntent.getActivity(
            this, 0, mIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = Notification.Builder(this).setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(iconId)
            .setTicker("").setContentTitle(msgtitle)
            .setContentText(msgcontent).setDefaults(Notification.DEFAULT_SOUND)
            .setContentIntent(resultPendingIntent).build()
        startForeground(nofityId, notification)
    }

    private fun initNotifyO() {
        val packageName = packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = packageName
            val CHANNEL_NAME = "$packageName.MyService"
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val msgtitle = getString(R.string.app_name)
            var msgcontent = "VpSdk 启动前台服务通知"
            msgcontent = String.format(msgcontent, msgtitle)
            val notification = Notification.Builder(
                applicationContext, CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msgtitle)
                .setContentText(msgcontent)
                .build()
            startForeground(1, notification)
        }
    }


    fun getCurProcessName(context: Context): String? {
        val pid = Process.myPid()
        val mActivityManager = context
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in mActivityManager
            .runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Suppress("deprecation")
    fun startScanBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isEnable) {
            Log.e("BluetoothLESearcher", "startScanDevice-------------> 版本>=26 开始搜索")
            startScanDevice2()
        } else {
            Log.e(
                "BluetoothLESearcher",
                "startScanBluetooth------------->startLeScan 低版本 开始搜索"
            )
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Suppress("deprecation")
    fun stopScanBluetooth() {
        // TODO Auto-generated method stub
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isEnable) {
                Log.e("BluetoothLESearcher", "stopScanBluetooth-------------> 版本>=26 搜索停止")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mScanner!!.stopScan(mScanCallback)
            } else {
                Log.e(
                    "BluetoothLESearcher",
                    "stopScanBluetooth------------->stopLeScan 低版本 搜索停止"
                )
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            }
        } catch (e: Exception) {
            BluetoothLog.e(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Suppress("deprecation")
    protected fun cancelScanBluetooth() {
        // TODO Auto-generated method stub

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isEnable) {
            Log.e("BluetoothLESearcher", "cancelScanBluetooth-------------> 版本>=26 搜索停止")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mScanner!!.stopScan(mScanCallback)
        } else {
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            Log.e("BluetoothLESearcher", "cancelScanBluetooth-------------> 低版本 搜索停止")
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun startScanDevice2() {
        Log.e("BluetoothLESearcher", "开始搜索设备-------------> ")
        scan()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mBluetoothAdapter!!.bluetoothLeScanner.startScan(
            buildScanFilters(),
            mScanSettings,
            mScanCallback
        )
    }

    //设置蓝牙扫描过滤器集合
    private var scanFilterList: MutableList<ScanFilter>? = null

    //设置蓝牙扫描过滤器
    private var scanFilterBuilder: ScanFilter.Builder? = null

    //设置蓝牙扫描设置
    private var scanSettingBuilder: ScanSettings.Builder? = null

    @TargetApi(Build.VERSION_CODES.O)
    private fun buildScanFilters(): List<ScanFilter> {
        scanFilterList = ArrayList()
        // 通过服务 uuid 过滤自己要连接的设备   过滤器搜索GATT服务UUID
        scanFilterBuilder = ScanFilter.Builder()
        val parcelUuidMask = ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
        //        ParcelUuid parcelUuid = ParcelUuid.fromString("0000ff07-0000-1000-8000-00805f9b34fb");
        scanFilterBuilder!!.setServiceUuid(serviceUuid, parcelUuidMask)
        scanFilterBuilder!!.setServiceUuid(serviceUuid)
        (scanFilterList as ArrayList<ScanFilter>).add(scanFilterBuilder!!.build())

        (scanFilterList as ArrayList<ScanFilter>).add(
            ScanFilter.Builder() //过滤扫描蓝牙设备的主服务
                .setServiceUuid(ParcelUuid.fromString("0000ffff-0000-1000-8000-00805f9bfffb"))
                .build()
        )
        return scanFilterList as ArrayList<ScanFilter>
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun buildScanSettings(): ScanSettings {
        scanSettingBuilder = ScanSettings.Builder()
        //设置蓝牙LE扫描的扫描模式。
        //使用最高占空比进行扫描。建议只在应用程序处于此模式时使用此模式在前台运行
        scanSettingBuilder!!.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
        //在主动模式下，即使信号强度较弱，hw也会更快地确定匹配.在一段时间内很少有目击/匹配。
        scanSettingBuilder!!.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        //设置蓝牙LE扫描的回调类型
        //为每一个匹配过滤条件的蓝牙广告触发一个回调。如果没有过滤器是活动的，所有的广告包被报告
        scanSettingBuilder!!.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        scanSettingBuilder!!.setLegacy(true)
        return ScanSettings.Builder().build()
    }

    private val mLeScanCallback =
        LeScanCallback { device, rssi, scanRecord -> // TODO Auto-generated method stub

        }
    var serviceUuid: ParcelUuid = ParcelUuid.fromString("0000fee7-0000-1000-8000-00805f9b34fb")

    @TargetApi(Build.VERSION_CODES.O)
    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.e(
                "BluetoothLESearcher",
                "onScanResult-------------> results = $result"
            )
            if (result.scanRecord != null) {

            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            Log.e(
                TAG,
                "onBatchScanResults-------------> results = $results"
            )
            for (result in results) {

            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(
                "BluetoothLESearcher",
                "onScanFailed-------------> errorCode = $errorCode"
            )
        }
    }


    private var mScanSettings: ScanSettings? = null

    fun scan() {
        val front = isAppForeground(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (null == mScanner) {
                mScanner = mBluetoothAdapter!!.bluetoothLeScanner
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (front) {
                    if (null == mScanSettings) {
                        mScanSettings = ScanSettings.Builder() //前台设置扫描模式为低时延
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build()
                    }
                } else {
                    mScanSettings = ScanSettings.Builder() //退到后台时设置扫描模式为低能耗
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()
                }
            } else {
                if (front) {
                    if (null == mScanSettings) {
                        mScanSettings = ScanSettings.Builder() //前台设置扫描模式为低时延
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()
                    }
                } else {
                    mScanSettings = ScanSettings.Builder() //退到后台时设置扫描模式为低能耗
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
                }
            }
        }
    }

    private fun isAppForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfoList =
            activityManager.runningAppProcesses ?: return false

        for (processInfo in runningAppProcessInfoList) {
            if (processInfo.processName == context.packageName
                && (processInfo.importance ==
                        RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG: String = MyService::class.java.simpleName
    }
}