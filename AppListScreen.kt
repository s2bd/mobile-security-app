package com.dewanmukto.sechero

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import java.security.cert.X509Certificate
import android.content.pm.Signature
import java.security.cert.CertificateFactory
import android.util.Base64

val countryLookup = mapOf(
    "CN" to "China", "DE" to "Germany", "US" to "United States", "IN" to "India", "JP" to "Japan",
    "BR" to "Brazil", "FR" to "France", "GB" to "United Kingdom", "IT" to "Italy", "CA" to "Canada",
    "RU" to "Russia", "AU" to "Australia", "MX" to "Mexico", "KR" to "South Korea", "ES" to "Spain",
    "ID" to "Indonesia", "TR" to "Turkey", "SA" to "Saudi Arabia", "NG" to "Nigeria", "PK" to "Pakistan",
    "PH" to "Philippines", "EG" to "Egypt", "TH" to "Thailand", "AR" to "Argentina", "ZA" to "South Africa",
    "PL" to "Poland", "VN" to "Vietnam", "SE" to "Sweden", "NO" to "Norway", "FI" to "Finland",
    "DK" to "Denmark", "CH" to "Switzerland", "BE" to "Belgium", "AT" to "Austria", "GR" to "Greece",
    "PT" to "Portugal", "HU" to "Hungary", "RO" to "Romania", "KR" to "South Korea", "MY" to "Malaysia",
    "SG" to "Singapore", "CL" to "Chile", "CO" to "Colombia", "UA" to "Ukraine", "IQ" to "Iraq",
    "BD" to "Bangladesh", "KW" to "Kuwait", "QA" to "Qatar", "JO" to "Jordan", "LB" to "Lebanon",
    "MA" to "Morocco", "LY" to "Libya", "AL" to "Albania", "RS" to "Serbia", "HR" to "Croatia",
    "SI" to "Slovenia", "MK" to "North Macedonia", "MD" to "Moldova", "GE" to "Georgia", "AM" to "Armenia",
    "AZ" to "Azerbaijan", "BY" to "Belarus", "UZ" to "Uzbekistan", "KZ" to "Kazakhstan", "TJ" to "Tajikistan",
    "KG" to "Kyrgyzstan", "TM" to "Turkmenistan", "AF" to "Afghanistan", "SY" to "Syria", "NP" to "Nepal",
    "BT" to "Bhutan", "MV" to "Maldives", "LA" to "Laos", "KH" to "Cambodia", "MM" to "Myanmar",
    "MN" to "Mongolia", "KH" to "Cambodia", "EE" to "Estonia", "LV" to "Latvia", "LT" to "Lithuania",
    "FI" to "Finland", "IE" to "Ireland", "IS" to "Iceland", "MT" to "Malta", "LU" to "Luxembourg",
    "MC" to "Monaco", "AD" to "Andorra", "SM" to "San Marino", "VA" to "Vatican City", "GI" to "Gibraltar",
    "FO" to "Faroe Islands", "GG" to "Guernsey", "JE" to "Jersey", "IM" to "Isle of Man",
    "BL" to "Saint Barthélemy", "MF" to "Saint Martin", "SX" to "Sint Maarten", "GP" to "Guadeloupe",
    "MQ" to "Martinique", "RE" to "Réunion", "PM" to "Saint Pierre and Miquelon", "WF" to "Wallis and Futuna",
    "PF" to "French Polynesia", "NC" to "New Caledonia", "YT" to "Mayotte", "CK" to "Cook Islands",
    "NU" to "Niue", "TO" to "Tonga", "WS" to "Samoa", "VU" to "Vanuatu", "FJ" to "Fiji",
    "SB" to "Solomon Islands", "PG" to "Papua New Guinea", "KI" to "Kiribati", "FM" to "Federated States of Micronesia",
    "MH" to "Marshall Islands", "NR" to "Nauru", "TV" to "Tuvalu", "AS" to "American Samoa",
    "GU" to "Guam", "MP" to "Northern Mariana Islands", "PW" to "Palau", "FM" to "Federated States of Micronesia"
)

@Composable
fun AppListScreen(navController: NavController) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    var selectedApp by remember { mutableStateOf<ApplicationInfo?>(null) }

    LazyColumn(modifier = Modifier.padding(10.dp)) {
        items(apps) { app ->
            AppItem(app, packageManager) { selectedApp = app }
        }
    }

    selectedApp?.let {
        AppDetailsDialog(it, packageManager) { selectedApp = null }
    }
}

@Composable
fun AppItem(app: ApplicationInfo, packageManager: PackageManager, onClick: () -> Unit) {
    val appName = packageManager.getApplicationLabel(app).toString()
    val appIcon = packageManager.getApplicationIcon(app)

    val permissions = try {
        val packageInfo = packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
        packageInfo.requestedPermissions?.size ?: 0
    } catch (e: Exception) {
        0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = appName,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(appName, modifier = Modifier.weight(1f), fontSize = 16.sp)
            Text(" 🔐 $permissions", fontSize = 16.sp)
        }
    }
}

@Composable
fun AppDetailsDialog(app: ApplicationInfo, packageManager: PackageManager, onDismiss: () -> Unit) {
    val appName = packageManager.getApplicationLabel(app).toString()
    val appIcon = packageManager.getApplicationIcon(app)

    val permissions = try {
        val packageInfo = packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
        packageInfo.requestedPermissions?.toList() ?: listOf("No permissions requested")
    } catch (e: Exception) {
        listOf("No permissions available")
    }

    val installDate = try {
        val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(packageInfo.firstInstallTime))
    } catch (e: Exception) {
        "Unknown"
    }

    val developer = try {
        packageManager.getInstallerPackageName(app.packageName) ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }

    val certificateIssuerCountry = try {
        val certs = packageManager.getPackageInfo(app.packageName, PackageManager.GET_SIGNATURES).signatures
        val cert = certs?.firstOrNull()?.let {
            val certFactory = CertificateFactory.getInstance("X.509")
            val x509Cert = certFactory.generateCertificate(it.toByteArray().inputStream()) as X509Certificate
            val countryCode = x509Cert.issuerDN.name.split(",").find { it.trim().startsWith("C=") }?.substring(2)?.trim()
            countryCode?.let { "$countryCode (${countryLookup[it] ?: "Unknown"})" } ?: "Unknown"
        }
        cert ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(appName, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Permissions:")
                permissions.forEach { permission ->
                    val color = when {
                        permission.contains("INTERNET") || permission.contains("READ_SMS") || permission.contains("ACCESS_FINE_LOCATION") ||
                                permission.contains("CAMERA") || permission.contains("RECORD_AUDIO") || permission.contains("SEND_SMS") ||
                                permission.contains("ACCESS_COARSE_LOCATION") || permission.contains("READ_CONTACTS") ||
                                permission.contains("READ_CALL_LOG") || permission.contains("WRITE_CALL_LOG") || permission.contains("CALL_PHONE") ||
                                permission.contains("WRITE_CONTACTS") || permission.contains("ACCESS_WIFI_STATE") || permission.contains("CHANGE_WIFI_STATE") ||
                                permission.contains("ACCESS_BACKGROUND_LOCATION") -> Color.Red  // Critical/Dangerous Permissions (Red)

                        permission.contains("VIBRATE") || permission.contains("ACCESS_NETWORK_STATE") || permission.contains("CHANGE_NETWORK_STATE") ||
                                permission.contains("READ_EXTERNAL_STORAGE") || permission.contains("WRITE_EXTERNAL_STORAGE") ||
                                permission.contains("SET_WALLPAPER") || permission.contains("SET_WALLPAPER_HINTS") || permission.contains("BLUETOOTH") ||
                                permission.contains("BLUETOOTH_ADMIN") || permission.contains("BLUETOOTH_PRIVILEGED") || permission.contains("ACCESS_FINE_LOCATION") ||
                                permission.contains("ACCESS_COARSE_LOCATION") -> Color(0xFFFFA500) // Sensitive Permissions (Orange)

                        else -> Color(0xFF6E8793)  // Normal/Safe Permissions (Blue)
                    }
                    Text(permission, color = color)
                }


                Spacer(modifier = Modifier.height(10.dp))
                Text("Publisher: $developer")
                Text("Package: ${app.packageName}")
                Text("Installed on: $installDate")
                Text("Certificate Issuer Country: $certificateIssuerCountry")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
