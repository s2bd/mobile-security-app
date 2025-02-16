package com.dewanmukto.sechero

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.dewanmukto.sechero.ui.theme.SecHeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the Foreground Service when the app starts
        startForegroundService()

        setContent {
            SecHeroTheme {
                MainScreen()
            }
        }
    }

    // Function to start the Foreground Service
    private fun startForegroundService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent) // For Android O and above
        } else {
            startService(serviceIntent) // For older versions
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "status",
            modifier = Modifier.padding(padding)
        ) {
            composable("status") { StatusScreen(navController) }
            composable("apps") { AppListScreen(navController) }
            composable("history") { HistoryScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf("Status", "Apps", "History", "Settings")
    val icons = listOf("ℹ️", "📱", "📜", "⚙️") // Emojis for each screen
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                selected = (currentDestination == screen),
                onClick = { navController.navigate(screen) },
                icon = { Text(items[index]) },
                label = { Text(icons[index]) }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text)
    }
}
