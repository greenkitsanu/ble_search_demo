package com.example.ble

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.ble.ui.theme.BLETheme
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.veepoo.protocol.VPOperateManager

data class BlModel(
    val name: String,
    val address: String
)

class MainActivity : ComponentActivity() {
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        VPOperateManager.getInstance().init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 200
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(

                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), 200
            )
        }

        ActivityCompat.requestPermissions(
            this, arrayOf<String>(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ), 200
        )
        setContent {
            BLETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    MainScreen()
                }
            }
        }
    }
}


@Composable
fun MainScreen() {
    //kotlin code
    var status by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    var list = remember {
        mutableStateListOf<BlModel>()
    }
    val mClient = BluetoothClient(context);
    val request = SearchRequest.Builder()
        .searchBluetoothLeDevice(3000, 3) // 先扫BLE设备3次，每次3s
        .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
        .searchBluetoothLeDevice(2000) // 再扫BLE设备2s
        .build()
    MainScreenContent(
        status = status,
        callbackStart = {

            mClient.search(request, object : SearchResponse {
                override fun onSearchStarted() {
                    status = "onSearchStarted"
                }

                override fun onDeviceFounded(device: SearchResult) {
                    status = "onDeviceFounded"
                    val beacon = Beacon(device.scanRecord)
                    device.name
                    BluetoothLog.v(
                        String.format(
                            "beacon for %s\n%s\n%s",
                            device.address,
                            device.name,
                            beacon.toString()
                        )
                    )

                    list.find { f -> f.address != device.address }?.let {
                        list.add(BlModel(name = device.name, address = device.address))
                    } ?: run {
                        list.add(BlModel(name = device.name, address = device.address))
                    }

                }

                override fun onSearchStopped() {
                    status = "onSearchStopped"
                }

                override fun onSearchCanceled() {
                    status = "onSearchCanceled"
                }
            })
        },
        callbackStop = {
            mClient.stopSearch()
        },
        callbackClear = {
            list.clear()
        },
        listBL = list
    )


}

@Composable
fun MainScreenContent(
    status: String = "",
    callbackStart: () -> Unit = {},
    callbackStop: () -> Unit = {},
    callbackClear: () -> Unit = {},
    listBL: List<BlModel> = arrayListOf()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 56.dp
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status,
            modifier = Modifier
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    callbackStart()
                }
            ) {
                Text("start")
            }
            Button(
                onClick = {
                    callbackStop()
                }
            ) {
                Text("stop")
            }
            Button(
                onClick = {
                    callbackClear()
                }
            ) {
                Text("clear")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(listBL) {
                Text("name" + it.name + " address" + it.address)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BLETheme {
        MainScreenContent()
    }
}