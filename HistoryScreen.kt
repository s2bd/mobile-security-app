package com.dewanmukto.sechero

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val threatHistory = remember { getThreatHistory(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Threat History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (threatHistory.isEmpty()) {
            Text("No threats detected yet.", fontSize = 16.sp, color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp)
            ) {
                items(threatHistory) { timestamp ->
                    HistoryItem(timestamp)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(timestamp: Long) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2))
    ) {
        Text(
            text = "⚠️ Threat detected on $formattedDate",
            fontSize = 16.sp,
            modifier = Modifier.padding(10.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

fun getThreatHistory(context: Context): List<Long> {
    val sharedPrefs = context.getSharedPreferences("ThreatHistory", Context.MODE_PRIVATE)
    return sharedPrefs.all.keys
        .filter { it.startsWith("threat_") }
        .mapNotNull { sharedPrefs.getLong(it, 0L).takeIf { it > 0 } }
        .sortedDescending() // Sort by most recent threats first
}
