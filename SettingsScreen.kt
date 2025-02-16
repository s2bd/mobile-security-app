package com.dewanmukto.sechero

import android.content.Context
import android.os.Build
import java.io.File
import android.os.StatFs
import android.os.BatteryManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Getting device information
    val hardwareInfo = getHardwareInfo(context)
    val systemInfo = getSystemInfo()
    val batteryInfo = getBatteryInfo(context)
    val networkInfo = getNetworkInfo(context)
    val cameraInfo = getCameraInfo(context)

    // Device info categories
    val deviceInfoCategories = listOf(
        "Hardware" to hardwareInfo,
        "System" to systemInfo,
        "Battery" to batteryInfo,
        "Network" to networkInfo,
        "Camera" to cameraInfo
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            // Device Info Panel
            Text("Device Info", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    deviceInfoCategories.forEach { (category, info) ->
                        val isExpanded = expandedCategory == category
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        expandedCategory = if (isExpanded) null else category
                                    }
                            ) {
                                Text(category, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                            if (isExpanded) {
                                Text(info, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            // Credits Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("About this app", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        "SecHero © 2025\nAsif Imtiaz Chowdhury, Fatema Tuz Zohora Panna, MD Rezaul Karim, Nafiz Ahmed Rhythm\n" +
                                "Made as a prototype for our thesis at BRAC University",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Function to get hardware info
fun getHardwareInfo(context: Context): String {
    val stat = StatFs(File("/data").absolutePath)
    val totalStorage = stat.blockCountLong * stat.blockSizeLong / (1024 * 1024 * 1024)
    val availableStorage = stat.availableBlocksLong * stat.blockSizeLong / (1024 * 1024 * 1024)
    return "Device: ${Build.MODEL} ${Build.DEVICE}\n" +
            "CPU: ${Build.HARDWARE}, Cores: ${Runtime.getRuntime().availableProcessors()}\n" +
            "Storage: ${availableStorage}GB/${totalStorage}GB free"
}

// Function to get system info
fun getSystemInfo(): String {
    return "OS: Android ${Build.VERSION.RELEASE}\n" +
            "Build: ${Build.ID}\n" +
            "API Level: ${Build.VERSION.SDK_INT}\n" +
            "Security Patch: ${Build.VERSION.SECURITY_PATCH}\n" +
            "Kernel: ${System.getProperty("os.version")}"
}

fun getBatteryInfo(context: Context): String {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    // Get battery percentage and charge counter
    val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) // µAh

    // Convert charge counter to mAh
    val chargeCountermAh = if (chargeCounter > 0) chargeCounter / 1000 else -1

    // Estimate actual full capacity in mAh
    val estimatedCapacity = if (chargeCountermAh > 0) (chargeCountermAh * 100) / batteryPercentage else -1

    // Calculate battery wear percentage
    val batteryWear = if (estimatedCapacity > 0) (estimatedCapacity.toFloat()) * 100 else -1f

    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val batteryHealth = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
    val batteryTemperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10.0) ?: 0.0
    val batteryVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val batteryTechnology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

    return "Battery: $batteryPercentage%\n" +
            "Charge Counter: ${if (chargeCountermAh > 0) "$chargeCountermAh mAh" else "Unavailable"}\n" +
            "Estimated Capacity: ${if (estimatedCapacity > 0) "$estimatedCapacity mAh" else "Unavailable"}\n" +
            "Health: ${getBatteryHealthDescription(batteryHealth)}\n" +
            "Temperature: ${batteryTemperature}°C\n" +
            "Voltage: ${batteryVoltage}mV\n" +
            "Technology: $batteryTechnology"
}

// Function to get network info
fun getNetworkInfo(context: Context): String {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    return if (capabilities != null) {
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi: Connected"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data: Connected"
            else -> "No active network"
        }
    } else {
        "No active network"
    }
}

// Function to get camera info
fun getCameraInfo(context: Context): String {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraIds = cameraManager.cameraIdList
    return "Number of cameras: ${cameraIds.size}"
}

// Function to get total RAM (in GB) for the hardware info
fun getTotalRAM(): String {
    val memoryClass = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024)
    return memoryClass.toString()
}

// Function to interpret battery health
fun getBatteryHealthDescription(health: Int): String {
    return when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        else -> "Unknown"
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(navController = rememberNavController())
}
