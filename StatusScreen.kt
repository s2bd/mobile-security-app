package com.dewanmukto.sechero

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun StatusScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("StatusScreenPrefs", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    // Load stored progress
    var progress by remember { mutableStateOf(sharedPrefs.getInt("progress", 0)) }
    var thirdPartyStatus by remember { mutableStateOf<Boolean?>(null) }
    var showPopUp by remember { mutableStateOf(false) }
    var thirdPartyApps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }

    val buttons = listOf(
        Triple("Set password PIN", 20, Intent(Settings.ACTION_SECURITY_SETTINGS)),
        Triple("Set biometric PIN", 25, Intent(Settings.ACTION_SECURITY_SETTINGS)),
        Triple("No 3rd party apps", 25, null),
        Triple("Latest security version", 30, Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val bubbleColor = Color.Green.copy(alpha = progress / 100f)
        val textColor = if (progress > 49) Color.Black else Color.White

        val wobble by animateFloatAsState(
            targetValue = if (progress > 0) 1.2f else 1f,
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer(scaleX = wobble, scaleY = wobble)
                .background(bubbleColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$progress%", fontSize = 30.sp, color = textColor)
        }

        Spacer(modifier = Modifier.height(20.dp))

        buttons.forEachIndexed { index, (label, value, intent) ->
            val buttonKey = "button_$index"
            var isCompleted by remember { mutableStateOf(sharedPrefs.getBoolean(buttonKey, false)) }

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                SecurityButton(
                    label = label,
                    value = value,
                    intent = intent,
                    thirdPartyStatus = thirdPartyStatus,
                    thirdPartyApps = thirdPartyApps,
                    completed = isCompleted,
                    onShowPopUpChange = { showPopUp = it },
                    onThirdPartyCheck = {
                        coroutineScope.launch {
                            thirdPartyApps = checkForThirdPartyApps(context)
                            thirdPartyStatus = thirdPartyApps.isEmpty()
                            if (!thirdPartyStatus!!) {
                                showPopUp = true
                                saveThreatHistory(context)
                            }
                        }
                    },
                    onCompleted = { increase ->
                        if (!isCompleted) {
                            progress += value
                            isCompleted = true
                            sharedPrefs.edit().putBoolean(buttonKey, true).putInt("progress", progress).apply()
                        }
                    }
                )
            }
        }

        if (showPopUp) {
            AlertDialog(
                onDismissRequest = { showPopUp = false },
                title = {
                    Text("⚠️ Threats found!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Some apps were installed externally, not from Google Play Store!")
                        Spacer(modifier = Modifier.height(8.dp))
                        thirdPartyApps.forEach { app ->
                            Text(
                                text = "• ${context.packageManager.getApplicationLabel(app)}",
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showPopUp = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun SecurityButton(
    label: String,
    value: Int,
    intent: Intent?,
    thirdPartyStatus: Boolean?,
    thirdPartyApps: List<ApplicationInfo>,
    completed: Boolean,
    onShowPopUpChange: (Boolean) -> Unit,
    onThirdPartyCheck: () -> Unit,
    onCompleted: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(false) }

    val buttonColor by animateColorAsState(
        targetValue = when {
            completed -> Color.Green
            checked && thirdPartyStatus == false && label == "No 3rd party apps" -> Color(0xFFFF9800)
            checked && thirdPartyStatus == true -> Color.Green
            else -> Color.Gray
        },
        animationSpec = tween(durationMillis = 500)
    )

    Button(
        onClick = {
            if (!completed) {
                if (label == "No 3rd party apps") {
                    onThirdPartyCheck()
                    checked = true
                } else {
                    onCompleted(true)
                    intent?.let { context.startActivity(it) }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                color = Color.White,
                textAlign = TextAlign.Start,
                fontSize = 16.sp
            )

            if (!completed) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$value",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text("✔", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

fun saveThreatHistory(context: Context) {
    val sharedPrefs = context.getSharedPreferences("ThreatHistory", Context.MODE_PRIVATE)
    val timestamp = System.currentTimeMillis()
    sharedPrefs.edit().putLong("threat_$timestamp", timestamp).apply()
}

fun checkForThirdPartyApps(context: Context): List<ApplicationInfo> {
    val packageManager = context.packageManager
    val currentAppPackage = context.packageName // Get the current app's package name

    return packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter { app ->
        // Skip the current app
        if (app.packageName == currentAppPackage) {
            return@filter false
        }

        // Check if the app is a system app or not
        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        // Check if the app is sideloaded
        val isSideloaded = app.sourceDir.startsWith("/data/app/") && !isSystemApp && !isUpdatedSystemApp

        // Track the installer package name
        val installerPackageName = try {
            packageManager.getInstallerPackageName(app.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        // Determine if the app is installed from Google Play Store or other sources
        val isFromPlayStore = installerPackageName == "com.android.vending"
        val isFromOtherSource = installerPackageName != null && installerPackageName != "com.android.vending" && installerPackageName != "android"

        // Filter and return sideloaded apps or apps installed from other sources
        isSideloaded && isFromOtherSource
    }
}


